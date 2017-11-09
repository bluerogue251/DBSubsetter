package trw.dbsubsetter

import java.sql.DriverManager

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl.GraphDSL.Implicits._
import akka.stream.scaladsl.{Balance, Broadcast, Flow, GraphDSL, Merge, RunnableGraph, Source}

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

    val baseQueryDbRequests = config.baseQueries.map { case ((schemaName, tableName), whereClause) =>
      val table = schemaInfo.tablesByName((schemaName, tableName))
      val (sqlString, cols) = SqlStatementMaker.makeSqlString(table, whereClause, schemaInfo, includeChildren = true)
      SqlString(table, cols, sqlString)
    }

    val dbSubsettingGraph = RunnableGraph.fromGraph(GraphDSL.create() { implicit b =>
      val fkToDb: Flow[FkTask, FkQuery, NotUsed] = Flow[FkTask].filterNot(_.fk.pointsToPk).map(FkQuery)
      val fkToPkExists: Flow[FkTask, PkExists, NotUsed] = Flow[FkTask].filter(_.fk.pointsToPk).map(t => PkExists(s"From $t"))

      val dbRequestToResult: Flow[DbRequest, DbResult, NotUsed] = Flow[DbRequest].statefulMapConcat { () =>
        val db = new DbAccess(config.originDbConnectionString, config.targetDbConnectionString, schemaInfo)
        req => {
          val res = req match {
            case FkQuery(t: FkTask) =>
              DbResult(req, db.getRowsFromTemplate(t.fk, t.table, t.fetchChildren, t.values))
            case SqlString(_, columns, sql) =>
              DbResult(req, db.getRows(sql, columns))
            case Copy(pk, pkValues) =>
              db.copyToTargetDB(pk, pkValues)
              DbResult(req, Seq.empty)
          }
          List(res)
        }
      }

      val dbResultToPkAdd: Flow[DbResult, PkAdd, NotUsed] = Flow[DbResult].collect { case DbResult(req: DbFetch) => req }.map(req => PkAdd(s"from $req"))
      val pkQueryToResult: Flow[PkRequest, PkQueryResult, NotUsed] = Flow[PkRequest].statefulMapConcat { () =>
        val primaryKeyMap = Seq("foo", "bar", "baz").map(_ -> mutable.HashSet.empty[Vector[Any]]).toMap
        request => {
          request match {
            case req@PkExists(_) => List(PkQueryResult(req, primaryKeyMap("baz").contains(Vector("baz"))))
            case req@PkAdd(_) => List(PkQueryResult(req, primaryKeyMap("baz").add(Vector("baz"))))
          }
        }
      }
      val pkExistsToDb: Flow[PkQueryResult, FkQuery, NotUsed] = Flow[PkQueryResult]
        .collect { case r@PkQueryResult(PkExists(_), false) => r }
        .map(psr => FkQuery(s"From $psr"))
      val pkAdded: Flow[PkQueryResult, PkQueryResult, NotUsed] = Flow[PkQueryResult].collect { case r@PkQueryResult(PkAdd(_), false) => r }
      val pkAddedToFkTask: Flow[PkQueryResult, FkTask, NotUsed] = Flow[PkQueryResult].map(psr => FkTask(s"From $psr", true, true))
      val pkAddedToDbCopy: Flow[PkQueryResult, Copy, NotUsed] = Flow[PkQueryResult].map(psr => Copy(s"From $psr"))

      // Merges and Broadcasts
      val mergeDbRequests = b.add(Merge[DbRequest](4))
      val balanceDbQueries = b.add(Balance[DbRequest](config.dbParallelism))
      val broadcastFkTasks = b.add(Broadcast[FkTask](2))
      val mergeToPkRequest = b.add(Merge[PkRequest](config.dbParallelism + 1))
      val splitPkResults = b.add(Broadcast[PkQueryResult](2))
      val broadcastPkAdded = b.add(Broadcast[PkQueryResult](2))

      // Merging all database query requests to allow for balancing them
      Source(baseQueryDbRequests) ~> mergeDbRequests.in(0)
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

sealed trait Task

case class FkTask(table: Table, fk: ForeignKey, values: Seq[Vector[AnyRef]], fetchChildren: Boolean) extends Task

sealed trait DbRequest

sealed trait DbFetch extends DbRequest

sealed trait DbCopy extends DbRequest

case class FkQuery(task: FkTask) extends DbFetch

case class SqlString(table: Table, cols: Seq[Column], sql: SqlQuery) extends DbFetch

case class Copy(pk: PrimaryKey, pkValues: Set[Vector[AnyRef]]) extends DbCopy

case class DbResult(request: DbRequest, rows: Seq[Row])


sealed trait PkRequest

case class PkExists(name: String) extends PkRequest

case class PkAdd(name: String) extends PkRequest

case class PkQueryResult(request: PkRequest, existed: Boolean)