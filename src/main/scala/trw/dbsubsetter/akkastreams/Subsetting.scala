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
import trw.dbsubsetter.workflow._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object Subsetting {
  def source(config: Config, schemaInfo: SchemaInfo, baseQueries: Vector[BaseQuery], pkStore: ActorRef, dbAccessFactory: DbAccessFactory)(implicit ec: ExecutionContext): Source[TargetDbInsertResult, NotUsed] = Source.fromGraph(GraphDSL.create() { implicit b =>
    // Infrastructure: Timeouts, Merges, and Broadcasts
    implicit val askTimeout: Timeout = Timeout(48.hours)
    val mergeOriginDbRequests = b.add(Merge[OriginDbRequest](3))
    val balanceOriginDb = b.add(Balance[OriginDbRequest](config.originDbParallelism, waitForAllDownstreams = true))
    val mergeOriginDbResults = b.add(Merge[OriginDbResult](config.originDbParallelism))
    val partitionOriginDbResults = b.add(Partition[OriginDbResult](2, res => if (res.table.storePks) 1 else 0))
    val partitionFkTasks = b.add(Partition[ForeignKeyTask](2, {
      case t: FetchParentTask if TaskPreCheck.shouldPrecheck(t) => 1
      case _ => 0
    }))
    val broadcastPkExistResult = b.add(Broadcast[PkResult](2))
    val mergePksAdded = b.add(Merge[PksAdded](2))
    val broadcastPksAdded = b.add(Broadcast[PksAdded](2))
    val mergeNewTaskRequests = b.add(Merge[PkResult](2))
    val balanceTargetDb = b.add(Balance[PksAdded](config.targetDbParallelism, waitForAllDownstreams = true))
    val mergeTargetDbResults = b.add(Merge[TargetDbInsertResult](config.targetDbParallelism))
    val fkTaskBufferFlow = b.add(new FkTaskBufferFlow(config, schemaInfo).async)

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
      mergeNewTaskRequests ~>
      NewTasks.flow(schemaInfo) ~>
      OutstandingTaskCounter.counter(baseQueries.size) ~>
      fkTaskBufferFlow

    broadcastPksAdded ~>
      Flow[PksAdded].buffer(config.preTargetBufferSize, OverflowStrategy.backpressure) ~>
      balanceTargetDb

    // ForeignKeyTasks ~> cannotBePrechecked       ~>        OriginDbRequest
    // ForeignKeyTasks ~> canBePrechecked ~> PkStoreQuery ~> OriginDbRequest
    //                                            ~> DuplicateTask
    fkTaskBufferFlow ~>
      partitionFkTasks

    partitionFkTasks.out(0) ~>
      mergeOriginDbRequests

    // TODO make Flow[ForeignKeyTask] more type specific -- it could actually be Flow[FetchParentTask]
    partitionFkTasks.out(1) ~>
      Flow[ForeignKeyTask].mapAsyncUnordered(10)(req => (pkStore ? req).mapTo[PkResult]) ~>
      broadcastPkExistResult

    broadcastPkExistResult ~>
      Flow[PkResult].collect { case f: ForeignKeyTask => f } ~>
      mergeOriginDbRequests

    broadcastPkExistResult ~>
      Flow[PkResult].collect { case DuplicateTask => DuplicateTask } ~>
      mergeNewTaskRequests

    SourceShape(mergeTargetDbResults.out)
  })
}
