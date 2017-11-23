package trw.dbsubsetter.akkastreams

import java.util.concurrent.TimeUnit

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.{ask, pipe}
import akka.stream.scaladsl.GraphDSL.Implicits._
import akka.stream.scaladsl.{Balance, Broadcast, Flow, GraphDSL, Merge, Source}
import akka.stream.{ActorMaterializer, OverflowStrategy, SourceShape}
import akka.util.Timeout
import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.SchemaInfo
import trw.dbsubsetter.workflow._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

object SubsettingSource {
  def source(config: Config, schemaInfo: SchemaInfo, baseQueries: List[SqlStrQuery], monitor: ActorRef, pkStore: ActorRef)(implicit sys: ActorSystem, mat: ActorMaterializer, ec: ExecutionContext): Source[TargetDbInsertResult, NotUsed] = Source.fromGraph(GraphDSL.create() { implicit b =>
    // Merges and Broadcasts
    implicit val askTimeout: Timeout = Timeout(FiniteDuration(100, TimeUnit.DAYS))
    val mergeOriginDbRequests = b.add(Merge[OriginDbRequest](3))
    val balanceOriginDb = b.add(Balance[OriginDbRequest](config.originDbParallelism, waitForAllDownstreams = true))
    val mergeDbResults = b.add(Merge[OriginDbResult](config.originDbParallelism))
    val broadcastFkTasks = b.add(Broadcast[FkTask](2))
    val broadcastPksAdded = b.add(Broadcast[PkResult](2))
    val balanceTargetDbInserts = b.add(Balance[PksAdded](config.targetDbParallelism, waitForAllDownstreams = true))
    val mergeTargetDbInsertResults = b.add(Merge[TargetDbInsertResult](config.targetDbParallelism))

    // Start everything off
    Source(baseQueries).watchTermination()((_, f) => f.map(d => ("baseQueries", d)) pipeTo monitor) ~> mergeOriginDbRequests

    // Processing Origin DB Queries in Parallel
    mergeOriginDbRequests.out ~> balanceOriginDb
    for (_ <- 0 until config.originDbParallelism) {
      balanceOriginDb ~> OriginDbQueryFlow.flow(config, schemaInfo).async.watchTermination()((_, f) => f.map(d => ("originDb", d)) pipeTo monitor) ~> mergeDbResults
    }

    // Processing Target DB Inserts in Parallel
    for (_ <- 1 to config.targetDbParallelism) {
      balanceTargetDbInserts ~> TargetDbInsertFlow.flow(config, schemaInfo).async.watchTermination()((_, f) => f.map(d => ("targetDb", d)) pipeTo monitor) ~> mergeTargetDbInsertResults
    }

    // DB Results ~> PkStoreAdd ~> NewTasks
    //                          ~> TargetDbInserts
    mergeDbResults ~>
      Flow[OriginDbResult].mapAsync(Int.MaxValue)(dbResult => (pkStore ? dbResult).mapTo[PksAdded]).watchTermination()((_, f) => f.map(d => ("pkStoreAddReqests", d)) pipeTo monitor) ~>
      broadcastPksAdded
    broadcastPksAdded ~>
      PkResultFlows.pkAddedToNewTasks(schemaInfo, baseQueries.size).watchTermination()((_, f) => f.map(d => ("pkAddedToNewTasks", d)) pipeTo monitor) ~>
      Flow[(Long, Vector[FkTask])].buffer(Int.MaxValue, OverflowStrategy.fail).watchTermination()((_, f) => f.map(d => ("preTripSwitchBuffer", d)) pipeTo monitor) ~>
      PkResultFlows.tripSwitch.watchTermination()((_, f) => f.map(d => ("tripSwitch", d)) pipeTo monitor) ~> broadcastFkTasks
    broadcastPksAdded ~>
      PkResultFlows.pkAddedToDbInsert(schemaInfo).watchTermination()((_, f) => f.map(d => ("pkAddedToDbInsert", d)) pipeTo monitor) ~>
      Flow[PksAdded].buffer(Int.MaxValue, OverflowStrategy.fail).watchTermination()((_, f) => f.map(d => ("preTargetDbBuffer", d)) pipeTo monitor) ~>
      balanceTargetDbInserts

    // FkTasks ~> PkStoreQuery ~> OriginDbRequest
    //         ~> OriginDbRequest
    broadcastFkTasks ~>
      FkTaskFlows.toPkStoreQuery.watchTermination()((_, f) => f.map(d => ("fkTaskToPkStoreQuery", d)) pipeTo monitor) ~>
      Flow[PkRequest].mapAsync(Int.MaxValue)(req => (pkStore ? req).mapTo[PkResult]).watchTermination()((_, f) => f.map(d => ("pkQueryAsync", d)) pipeTo monitor) ~>
      PkResultFlows.pkMissingToFkQuery.watchTermination()((_, f) => f.map(d => ("pkMissingToFkQuery", d)) pipeTo monitor) ~>
      mergeOriginDbRequests
    broadcastFkTasks ~>
      FkTaskFlows.toDbQuery.watchTermination()((_, f) => f.map(d => ("fkTaskToDbQuery", d)) pipeTo monitor) ~>
      mergeOriginDbRequests

    SourceShape(mergeTargetDbInsertResults.out)
  })
}
