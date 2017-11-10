package trw.dbsubsetter.akkastreams

import akka.NotUsed
import akka.stream.FlowShape
import akka.stream.scaladsl.GraphDSL.Implicits._
import akka.stream.scaladsl.{Balance, Broadcast, Flow, GraphDSL, Merge}
import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.SchemaInfo
import trw.dbsubsetter.workflow._

object SubsettingFlow {
  def flow(config: Config, schemaInfo: SchemaInfo): Flow[SqlStrQuery, DbCopyResult, NotUsed] = {
    Flow.fromGraph(GraphDSL.create() { implicit b =>
      // Merges and Broadcasts
      val mergeDbRequests = b.add(Merge[DbRequest](4))
      val balanceDbQueries = b.add(Balance[DbRequest](config.dbParallelism))
      val mergeDbResults = b.add(Merge[DbResult](config.dbParallelism))
      val broadcastDbResults = b.add(Broadcast[DbResult](2))
      val broadcastFkTasks = b.add(Broadcast[FkTask](2))
      val mergePkRequests = b.add(Merge[PkRequest](2))
      val broadcastPkResults = b.add(Broadcast[PkResult](3))

      // Merging all database query requests to allow for balancing them
      broadcastFkTasks ~> FkTaskFlows.toDbQuery ~> mergeDbRequests
      broadcastPkResults ~> PkResultFlows.pkMissingToFkQuery ~> mergeDbRequests
      broadcastPkResults ~> PkResultFlows.pkAddedToDbCopy(schemaInfo) ~> mergeDbRequests

      // Processing DB Queries in Parallel
      mergeDbRequests.out ~> balanceDbQueries
      for (_ <- 0 until config.dbParallelism) {
        balanceDbQueries ~> DbCallFlow.flow(config, schemaInfo).async ~> mergeDbResults
      }

      // Broadcast DB Results
      mergeDbResults ~> broadcastDbResults
      broadcastDbResults ~> DbResultFlows.toPkAddRequest ~> mergePkRequests
      val out = broadcastDbResults ~> DbResultFlows.toDbCopyResult

      mergePkRequests ~> PkStoreQueryFlow.flow(schemaInfo) ~> broadcastPkResults
      broadcastPkResults ~> PkResultFlows.pkAddedToNewTasks(schemaInfo) ~> broadcastFkTasks
      broadcastFkTasks ~> FkTaskFlows.toPkStoreQuery ~> mergePkRequests

      FlowShape(mergeDbRequests.in(3), out.outlet)
    })
  }
}
