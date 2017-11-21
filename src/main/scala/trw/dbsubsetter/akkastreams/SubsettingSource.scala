package trw.dbsubsetter.akkastreams

import akka.stream.scaladsl.GraphDSL.Implicits._
import akka.stream.scaladsl.{Balance, Broadcast, Flow, GraphDSL, Merge, Source}
import akka.stream.{OverflowStrategy, SourceShape}
import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.SchemaInfo
import trw.dbsubsetter.workflow._

object SubsettingSource {
  def source(config: Config, schemaInfo: SchemaInfo, baseQueries: List[SqlStrQuery]) = Source.fromGraph(GraphDSL.create() { implicit b =>
    // Merges and Broadcasts
    val mergeDbRequests = b.add(Merge[OriginDbRequest](3))
    val balanceDbQueries = b.add(Balance[OriginDbRequest](config.originDbParallelism, waitForAllDownstreams = true))
    val mergeDbResults = b.add(Merge[OriginDbResult](config.originDbParallelism))
    val broadcastFkTasks = b.add(Broadcast[FkTask](2))
    val mergePkRequests = b.add(Merge[PkRequest](2))
    val broadcastPkResults = b.add(Broadcast[PkResult](3))
    val balanceTargetDbInserts = b.add(Balance[PksAdded](config.targetDbParallelism, waitForAllDownstreams = true))
    val mergeTargetDbInsertResults = b.add(Merge[TargetDbInsertResult](config.targetDbParallelism))

    // Merging all database query requests to allow for balancing them
    broadcastFkTasks ~> FkTaskFlows.toDbQuery ~> mergeDbRequests
    broadcastPkResults ~> PkResultFlows.pkMissingToFkQuery ~> mergeDbRequests
    broadcastPkResults ~> PkResultFlows.pkAddedToDbInsert(schemaInfo) ~> Flow[PksAdded].buffer(Int.MaxValue, OverflowStrategy.fail) ~> balanceTargetDbInserts

    // Processing Origin DB Queries in Parallel
    mergeDbRequests.out ~> balanceDbQueries
    for (_ <- 0 until config.originDbParallelism) {
      balanceDbQueries ~> OriginDbQueryFlow.flow(config, schemaInfo).async ~> mergeDbResults
    }

    // Processing Target DB Inserts in Parallel
    for (_ <- 1 to config.targetDbParallelism) {
      balanceTargetDbInserts ~> TargetDbInsertFlow.flow(config, schemaInfo).async ~> mergeTargetDbInsertResults
    }

    // Broadcast DB Results
    mergeDbResults ~> mergePkRequests

    mergePkRequests ~> PkStoreQueryFlow.flow(schemaInfo.pkOrdinalsByTable) ~> broadcastPkResults
    broadcastPkResults ~> PkResultFlows.pkAddedToNewTasks(schemaInfo, baseQueries.size) ~> PkResultFlows.tripSwitch ~> Flow[FkTask].buffer(Int.MaxValue, OverflowStrategy.fail) ~> broadcastFkTasks
    broadcastFkTasks ~> FkTaskFlows.toPkStoreQuery ~> mergePkRequests

    SourceShape(mergeTargetDbInsertResults.out)
  })
}