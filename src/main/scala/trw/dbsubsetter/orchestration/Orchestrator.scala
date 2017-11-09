package trw.dbsubsetter.orchestration

import java.sql.DriverManager

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl.GraphDSL.Implicits._
import akka.stream.scaladsl.{Balance, Broadcast, Flow, GraphDSL, Merge, RunnableGraph, Source}
import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.{DbAccess, SchemaInfoRetrieval, Sql}

import scala.collection.mutable
import scala.concurrent.ExecutionContext

object Orchestrator {
  def doSubset(config: Config): Unit = {
    implicit val system: ActorSystem = ActorSystem("DbSubsetter")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val ec: ExecutionContext = system.dispatcher

    val schemaInfo = {
      val originConnForSchema = DriverManager.getConnection(config.originDbConnectionString)
      originConnForSchema.setReadOnly(true)
      SchemaInfoRetrieval.getSchemaInfo(originConnForSchema, config.schemas)
    }

    val startingDbRequests = config.baseQueries.map { case ((schemaName, tableName), whereClause) =>
      val table = schemaInfo.tablesByName((schemaName, tableName))
      val (sqlString, cols) = Sql.makeQueryString(table, whereClause, schemaInfo, includeChildren = true)
      SqlStrQuery(table, cols, sqlString)
    }

    val dbSubsettingGraph = RunnableGraph.fromGraph(GraphDSL.create() { implicit b =>
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

      val pkQueryToResult: Flow[PkRequest, PkResult, NotUsed] = Flow[PkRequest].statefulMapConcat { () =>
        val primaryKeyMap = Seq("foo", "bar", "baz").map(_ -> mutable.HashSet.empty[Vector[Any]]).toMap
        request => {
          request match {
            case req: PkExists => List(PkResult(req, primaryKeyMap("baz").contains(Vector("baz"))))
            case req: PkAdd => List(PkResult(req, primaryKeyMap("baz").add(Vector("baz"))))
          }
        }
      }
      val pkExistsToDb: Flow[PkResult, FkQuery, NotUsed] = Flow[PkResult].collect { case PkResult(PkExists(fkTask), false) => FkQuery(fkTask) }
      val pkAddedToFkTask: Flow[PkResult, PkResult, NotUsed] = Flow[PkResult]
        .collect { case r@PkResult(pka: PkAdd, false) => r }
      val pkAddedToFkTask: Flow[PkAdd, FkTask, NotUsed] = Flow[PkResult].map(pka => FkTask(pka.t,))
      val pkAddedToDbCopy: Flow[PkAdd, DbCopy, NotUsed] = Flow[PkResult].map(psr => DbCopy())

      // Merges and Broadcasts
      val mergeDbRequests = b.add(Merge[DbRequest](4))
      val balanceDbQueries = b.add(Balance[DbRequest](config.dbParallelism))
      val broadcastFkTasks = b.add(Broadcast[FkTask](2))
      val mergeToPkRequest = b.add(Merge[PkRequest](config.dbParallelism + 1))
      val splitPkResults = b.add(Broadcast[PkResult](2))
      val broadcastPkAdded = b.add(Broadcast[PkResult](2))

      // Merging all database query requests to allow for balancing them
      Source(startingDbRequests) ~> mergeDbRequests.in(0)
      broadcastFkTasks.out(0) ~> fkToDb ~> mergeDbRequests.in(1)
      splitPkResults.out(0) ~> pkExistsToDb ~> mergeDbRequests.in(2)
      broadcastPkAdded.out(1) ~> pkAddedToDbCopy ~> mergeDbRequests.in(3)

      // Processing DB Queries in Parallel
      mergeDbRequests.out ~> balanceDbQueries
      for (i <- 0 until config.dbParallelism) {
        balanceDbQueries.out(i) ~> dbRequestToResult.async ~> dbResultToPkAdd ~> mergeToPkRequest.in(i + 1)
      }

      broadcastFkTasks.out(1) ~> fkToPkExists ~> mergeToPkRequest.in(0)
      mergeToPkRequest.out ~> pkQueryToResult ~> splitPkResults
      splitPkResults.out(1) ~> pkAdded ~> broadcastPkAdded
      broadcastPkAdded.out(0) ~> pkAddedToFkTask ~> broadcastFkTasks.in

      ClosedShape
    })

    dbSubsettingGraph.run
  }
}