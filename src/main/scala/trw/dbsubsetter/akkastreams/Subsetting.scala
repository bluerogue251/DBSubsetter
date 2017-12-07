package trw.dbsubsetter.akkastreams

import akka.NotUsed
import akka.actor.ActorRef
import akka.pattern.ask
import akka.stream.scaladsl.GraphDSL.Implicits._
import akka.stream.scaladsl.{Balance, Broadcast, Flow, GraphDSL, Merge, Partition, Source}
import akka.stream.{OverflowStrategy, SourceShape}
import akka.util.Timeout
import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.SchemaInfo
import trw.dbsubsetter.workflow._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object Subsetting {
  def source(config: Config, schemaInfo: SchemaInfo, baseQueries: List[SqlStrQuery], pkStore: ActorRef)(implicit ec: ExecutionContext): Source[TargetDbInsertResult, NotUsed] = Source.fromGraph(GraphDSL.create() { implicit b =>
    // Infrastructure: Timeouts, Merges, and Broadcasts
    implicit val askTimeout: Timeout = Timeout(48.hours)
    val mergeOriginDbRequests = b.add(Merge[OriginDbRequest](3))
    val balanceOriginDb = b.add(Balance[OriginDbRequest](config.originDbParallelism, waitForAllDownstreams = true))
    val mergeOriginDbResults = b.add(Merge[OriginDbResult](config.originDbParallelism))
    val partitionOriginDbResults = b.add(Partition[OriginDbResult](2, res => if (res.table.storePks) 1 else 0))
    val partitionFkTasks = b.add(Partition[FkTask](2, t => if (FkTaskPreCheck.shouldPrecheck(t)) 1 else 0))
    val broadcastPkExistResult = b.add(Broadcast[PkResult](2))
    val mergePksAdded = b.add(Merge[PksAdded](2))
    val broadcastPksAdded = b.add(Broadcast[PksAdded](2))
    val mergeNewTaskRequests = b.add(Merge[PkResult](2))
    val balanceTargetDb = b.add(Balance[PksAdded](config.targetDbParallelism, waitForAllDownstreams = true))
    val mergeTargetDbResults = b.add(Merge[TargetDbInsertResult](config.targetDbParallelism))

    // Start everything off
    Source(baseQueries) ~> mergeOriginDbRequests

    // Process Origin DB Queries in Parallel
    mergeOriginDbRequests.out ~> balanceOriginDb
    for (_ <- 0 until config.originDbParallelism) {
      balanceOriginDb ~> OriginDb.query(config, schemaInfo).async ~> mergeOriginDbResults
    }

    // Process Target DB Inserts in Parallel
    for (_ <- 0 until config.targetDbParallelism) {
      balanceTargetDb ~> TargetDb.insert(config, schemaInfo).async ~> mergeTargetDbResults
    }

    // Origin DB Results ~> PkStoreAdd   ~>  |merge| ~> NewTasks
    //                   ~> DoNotStorePk ~>  |merge| ~> TargetDbInserts
    //
    //
    mergeOriginDbResults ~> partitionOriginDbResults

    partitionOriginDbResults.out(0) ~>
      Flow[OriginDbResult].map(SkipPkStore.process) ~>
      mergePksAdded

    partitionOriginDbResults.out(1) ~>
      Flow[OriginDbResult].mapAsync(500)(dbResult => (pkStore ? dbResult).mapTo[PksAdded]) ~>
      mergePksAdded

    mergePksAdded ~> broadcastPksAdded

    broadcastPksAdded ~>
      mergeNewTaskRequests ~>
      NewTasks.flow(schemaInfo, baseQueries.size) ~>
      partitionFkTasks

    broadcastPksAdded ~>
      Flow[PksAdded].buffer(Int.MaxValue, OverflowStrategy.backpressure) ~>
      balanceTargetDb

    // FkTasks ~> cannotBePrechecked       ~>        OriginDbRequest
    // FkTasks ~> canBePrechecked ~> PkStoreQuery ~> OriginDbRequest
    //                                            ~> DuplicateTask
    partitionFkTasks.out(0) ~> mergeOriginDbRequests

    partitionFkTasks.out(1) ~>
      Flow[FkTask].mapAsync(500)(req => (pkStore ? req).mapTo[PkResult]) ~>
      broadcastPkExistResult

    broadcastPkExistResult ~>
      Flow[PkResult].collect { case f: FkTask => f } ~>
      mergeOriginDbRequests

    broadcastPkExistResult ~>
      Flow[PkResult].collect { case DuplicateTask => DuplicateTask } ~>
      mergeNewTaskRequests

    SourceShape(mergeTargetDbResults.out)
  })
}
