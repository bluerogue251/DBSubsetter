package trw.dbsubsetter.orchestration

import akka.NotUsed
import akka.stream.SinkShape
import akka.stream.scaladsl.GraphDSL.Implicits._
import akka.stream.scaladsl.{Balance, Broadcast, Flow, GraphDSL, Merge, Sink}
import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.{DbAccess, SchemaInfo}

import scala.collection.mutable

object SubsettingProcessGraph {
  def getSink(config: Config, schemaInfo: SchemaInfo): Sink[SqlStrQuery, NotUsed] = {
    Sink.fromGraph(GraphDSL.create() { implicit b =>
      val fkToDb: Flow[FkTask, FkQuery, NotUsed] = Flow[FkTask].filterNot(_.fk.pointsToPk).map(FkQuery)
      val fkToPkExists: Flow[FkTask, PkExists, NotUsed] = Flow[FkTask].filter(_.fk.pointsToPk).map(PkExists)

      val dbRequestToResult: Flow[DbRequest, DbResult, NotUsed] = Flow[DbRequest].statefulMapConcat { () =>
        val db = new DbAccess(config.originDbConnectionString, config.targetDbConnectionString, schemaInfo)
        req => {
          val res = req match {
            case FkQuery(t: FkTask) =>
              DbResult(req, db.getRowsFromTemplate(t.fk, t.table, t.fetchChildren, t.values))
            case SqlStrQuery(_, columns, sql) =>
              DbResult(req, db.getRows(sql, columns))
            case DbCopy(pk, pkValues) =>
              db.copyToTargetDB(pk, pkValues)
              DbResult(req, Vector.empty)
          }
          List(res)
        }
      }

      val dbResultToPkAdd: Flow[DbResult, PkAdd, NotUsed] = Flow[DbResult]
        .collect { case DbResult(req: DbFetch, rows) => (req.table, rows) }
        .map { case (table, rows) => PkAdd(table, rows) }

      val filterAlreadyExistingPks: Flow[PkRequest, PkRequest, NotUsed] = Flow[PkRequest].statefulMapConcat { () =>
        val primaryKeyMap = schemaInfo.pksByTable.map { case (table, _) => table -> mutable.HashSet.empty[Vector[Any]] }
        request => {
          request match {
            case req@PkExists(fkTask) =>
              if (primaryKeyMap(fkTask.table).contains(fkTask.values)) List.empty[PkRequest] else List(req)
            case PkAdd(table, rows) =>
              val newRows = rows.filter { row =>
                val pkValues = schemaInfo.pksByTable(table).columns.map(row)
                primaryKeyMap(table).contains(pkValues)
              }
              List(PkAdd(table, newRows))
          }
        }
      }
      val pkExistsToDb = Flow[PkRequest].collect { case PkExists(fkTask) => FkQuery(fkTask) }
      val pkAddedToFkTask = Flow[PkRequest].collect { case PkAdd(table, rows) => FkTask(???, ???, ???, ???) }
      val pkAddedToDbCopy = Flow[PkRequest].collect { case PkAdd(table, rows) => DbCopy(???, ???) }

      // Merges and Broadcasts
      val mergeDbRequests = b.add(Merge[DbRequest](4))
      val balanceDbQueries = b.add(Balance[DbRequest](config.dbParallelism))
      val broadcastFkTasks = b.add(Broadcast[FkTask](2))
      val mergePkRequests = b.add(Merge[PkRequest](config.dbParallelism + 1))
      val broadcastPkResults = b.add(Broadcast[PkRequest](3))

      // Merging all database query requests to allow for balancing them
      broadcastFkTasks.out(0) ~> fkToDb ~> mergeDbRequests.in(1)
      broadcastPkResults.out(0) ~> pkExistsToDb ~> mergeDbRequests.in(2)
      broadcastPkResults.out(1) ~> pkAddedToDbCopy ~> mergeDbRequests.in(3)

      // Processing DB Queries in Parallel
      mergeDbRequests.out ~> balanceDbQueries
      for (i <- 0 until config.dbParallelism) {
        balanceDbQueries.out(i) ~> dbRequestToResult.async ~> dbResultToPkAdd ~> mergePkRequests.in(i + 1)
      }

      broadcastFkTasks.out(1) ~> fkToPkExists ~> mergePkRequests.in(0)
      mergePkRequests.out ~> filterAlreadyExistingPks ~> broadcastPkResults
      broadcastPkResults.out(2) ~> pkAddedToFkTask ~> broadcastFkTasks.in

      SinkShape(mergeDbRequests.in(0))
    })
  }
}
