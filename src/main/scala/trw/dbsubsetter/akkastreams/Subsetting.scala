package trw.dbsubsetter.akkastreams

import akka.NotUsed
import akka.actor.ActorRef
import akka.pattern.ask
import akka.stream.scaladsl.GraphDSL.Implicits._
import akka.stream.scaladsl.{Balance, Broadcast, Flow, GraphDSL, Merge, Partition, Source}
import akka.stream.{OverflowStrategy, SourceShape}
import akka.util.Timeout
import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.{DbAccessFactory, SchemaInfo}
import trw.dbsubsetter.workflow.offheap.OffHeapFkTaskQueue
import trw.dbsubsetter.workflow.{AlreadySeen, _}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object Subsetting {
  def source(config: Config, schemaInfo: SchemaInfo, baseQueries: Vector[BaseQuery], pkStore: ActorRef, dbAccessFactory: DbAccessFactory, fkTaskCreationWorkflow: FkTaskCreationWorkflow, fkTaskQueue: OffHeapFkTaskQueue)(implicit ec: ExecutionContext): Source[TargetDbInsertResult, NotUsed] = Source.fromGraph(GraphDSL.create() { implicit b =>
    // Infrastructure: Timeouts, Merges, and Broadcasts
    implicit val askTimeout: Timeout = Timeout(48.hours)
    val mergeOriginDbRequests = b.add(Merge[OriginDbRequest](3))
    val balanceOriginDb = b.add(Balance[OriginDbRequest](config.originDbParallelism, waitForAllDownstreams = true))
    val mergeOriginDbResults = b.add(Merge[OriginDbResult](config.originDbParallelism))
    val partitionOriginDbResults = b.add(Partition[OriginDbResult](2, res => if (res.table.storePks) 1 else 0))
    val partitionFkTasks = b.add(Partition[ForeignKeyTask](2, {
      case t: FetchParentTask => if (FkTaskPreCheck.shouldPrecheck(t)) 1 else 0
      case _ => 0
    }))
    // TODO try to turn this broadcast into a typesafe Partition stage with two output ports, each output port with a different type
    val broadcastPkExistResult = b.add(Broadcast[PkQueryResult](2))
    val mergePksAdded = b.add(Merge[PksAdded](2))
    val broadcastPksAdded = b.add(Broadcast[PksAdded](2))
    val balanceTargetDb = b.add(Balance[PksAdded](config.targetDbParallelism, waitForAllDownstreams = true))
    val mergeTargetDbResults = b.add(Merge[TargetDbInsertResult](config.targetDbParallelism))
    val fkTaskBufferFlow = b.add(new FkTaskBufferFlow(fkTaskQueue).async)
    val mergeToOustandingTaskCounter = b.add(Merge[NewTasks](2))

    // Start everything off
    Source(baseQueries) ~>
      mergeOriginDbRequests

    // Process Origin DB Queries in Parallel
    mergeOriginDbRequests.out ~> balanceOriginDb
    for (_ <- 0 until config.originDbParallelism) {
      balanceOriginDb ~> OriginDb.query(config, schemaInfo, dbAccessFactory).async ~> mergeOriginDbResults
    }

    // Process Target DB Inserts in Parallel
    for (_ <- 0 until config.targetDbParallelism) {
      balanceTargetDb ~> TargetDb.insert(config, schemaInfo, dbAccessFactory).async ~> mergeTargetDbResults
    }

    // Origin DB Results ~> PkStoreAdd  ~> |merge| ~> NewTasks
    //                   ~> SkipPkStore ~> |merge| ~> TargetDbInserts
    mergeOriginDbResults ~>
      partitionOriginDbResults

    partitionOriginDbResults.out(0) ~>
      Flow[OriginDbResult].map(SkipPkStore.process) ~>
      mergePksAdded

    partitionOriginDbResults.out(1) ~>
      Flow[OriginDbResult].mapAsyncUnordered(10)(dbResult => (pkStore ? dbResult).mapTo[PksAdded]) ~>
      mergePksAdded

    mergePksAdded ~>
      broadcastPksAdded

    broadcastPksAdded ~>
      FkTaskCreation.flow(fkTaskCreationWorkflow) ~>
      mergeToOustandingTaskCounter

    mergeToOustandingTaskCounter ~>
      OutstandingTaskCounter.counter(baseQueries.size) ~>
      fkTaskBufferFlow

    broadcastPksAdded ~>
      Flow[PksAdded].buffer(config.preTargetBufferSize, OverflowStrategy.backpressure) ~>
      balanceTargetDb

    // FkTasks ~> cannotBePrechecked       ~>        OriginDbRequest
    // FkTasks ~> canBePrechecked ~> PkStoreQuery ~> OriginDbRequest
    //                                            ~> DuplicateTask
    fkTaskBufferFlow ~>
      partitionFkTasks

    partitionFkTasks.out(0) ~>
      mergeOriginDbRequests

    partitionFkTasks.out(1) ~>
      Flow[ForeignKeyTask].mapAsyncUnordered(10)(req => (pkStore ? req).mapTo[PkQueryResult]) ~>
      broadcastPkExistResult

    broadcastPkExistResult ~>
      Flow[PkQueryResult].collect { case NotAlreadySeen(fkTask) => fkTask } ~>
      mergeOriginDbRequests

    broadcastPkExistResult ~>
      Flow[PkQueryResult].collect { case AlreadySeen => EmptyNewTasks } ~>
      mergeToOustandingTaskCounter

    SourceShape(mergeTargetDbResults.out)
  })
}
