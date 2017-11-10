package trw.dbsubsetter.akkastreams

import akka.stream.ClosedShape
import akka.stream.scaladsl.GraphDSL.Implicits._
import akka.stream.scaladsl.{Balance, Broadcast, GraphDSL, Merge, RunnableGraph, Source}
import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.SchemaInfo
import trw.dbsubsetter.workflow._

object SubsettingProcessGraph {
  def graph(config: Config, schemaInfo: SchemaInfo, baseQueries: List[SqlStrQuery]) = {
    RunnableGraph.fromGraph(GraphDSL.create() { implicit b =>
      // Merges and Broadcasts
      val mergeDbRequests = b.add(Merge[DbRequest](4))
      val balanceDbQueries = b.add(Balance[DbRequest](config.dbParallelism))
      val broadcastFkTasks = b.add(Broadcast[FkTask](2))
      val mergePkRequests = b.add(Merge[PkRequest](config.dbParallelism + 1))
      val broadcastPkResults = b.add(Broadcast[PkResult](3))

      // Merging all database query requests to allow for balancing them
      Source(baseQueries) ~> mergeDbRequests
      broadcastFkTasks ~> FkTaskFlows.toDbQuery ~> mergeDbRequests
      broadcastPkResults ~> PkMissingFlow.flow ~> mergeDbRequests
      broadcastPkResults ~> PkAddedFlows.pkAddedToDbCopyFlow(schemaInfo) ~> mergeDbRequests

      // Processing DB Queries in Parallel
      mergeDbRequests.out ~> balanceDbQueries
      for (_ <- 0 until config.dbParallelism) {
        balanceDbQueries ~> DbCallFlow.flow(config, schemaInfo).async ~> DbResultFlow.flow ~> mergePkRequests
      }

      broadcastFkTasks ~> FkTaskFlows.toPkStoreQuery ~> mergePkRequests
      mergePkRequests.out ~> PkStoreQueryFlow.flow(schemaInfo) ~> broadcastPkResults
      broadcastPkResults ~> PkAddedFlows.pkAddedToNewTasksFlow(schemaInfo) ~> broadcastFkTasks

      ClosedShape
    })
  }
}
