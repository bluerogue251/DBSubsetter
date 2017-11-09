package trw.dbsubsetter

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl.GraphDSL.Implicits._
import akka.stream.scaladsl.{Balance, Broadcast, Flow, GraphDSL, Merge, RunnableGraph, Source}

import scala.collection.mutable
import scala.concurrent.ExecutionContext

object SubsettingOrchestratorAkkaStreams {
  def doSubset(config: Config): Unit = {
    implicit val system: ActorSystem = ActorSystem("DbSubsetter")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val ec: ExecutionContext = system.dispatcher

    val baseQueryTasks: Source[InitialTask, NotUsed] = Source(List(
      InitialTask("BaseQueryTask-1", fetchChildren = true),
      InitialTask("BaseQueryTask-2", fetchChildren = true),
      InitialTask("BaseQueryTask-3", fetchChildren = false),
      InitialTask("BaseQueryTask-4", fetchChildren = true)
    ))

    val dbParallelism = 3

    val dbSubsettingGraph = RunnableGraph.fromGraph(GraphDSL.create() { implicit b =>
      // Flows
      val fkToDb: Flow[FkTask, PrepStmt, NotUsed] = Flow[FkTask].filter(_.fkPointsToPk).map(t => PrepStmt(s"From $t"))
      val fkToPkExists: Flow[FkTask, PkExists, NotUsed] = Flow[FkTask].filterNot(_.fkPointsToPk).map(t => PkExists(s"From $t"))
      val kickoffToDb: Flow[InitialTask, SqlString, NotUsed] = Flow[InitialTask].map(bq => SqlString(s"From $bq"))
      val dbToPkAdd: Flow[DbRequest, PkAdd, NotUsed] = Flow[DbRequest].map(dq => PkAdd(s"from $dq"))
      val pkQueryToResult: Flow[PkRequest, PkQueryResult, NotUsed] = Flow[PkRequest].statefulMapConcat { () =>
        val primaryKeyMap = Seq("foo", "bar", "baz").map(_ -> mutable.HashSet.empty[Vector[Any]]).toMap
        request => {
          request match {
            case req@PkExists(_) => List(PkQueryResult(req, primaryKeyMap("baz").contains(Vector("baz"))))
            case req@PkAdd(_) => List(PkQueryResult(req, primaryKeyMap("baz").add(Vector("baz"))))
          }
        }
      }
      val pkExistsToDb: Flow[PkQueryResult, PrepStmt, NotUsed] = Flow[PkQueryResult]
        .collect { case r@PkQueryResult(PkExists(_), false) => r }
        .map(psr => PrepStmt(s"From $psr"))
      val pkAdded: Flow[PkQueryResult, PkQueryResult, NotUsed] = Flow[PkQueryResult].collect { case r@PkQueryResult(PkAdd(_), false) => r }
      val pkAddedToFkTask: Flow[PkQueryResult, FkTask, NotUsed] = Flow[PkQueryResult].map(psr => FkTask(s"From $psr", true, true))
      val pkAddedToDbCopy: Flow[PkQueryResult, Copy, NotUsed] = Flow[PkQueryResult].map(psr => Copy(s"From $psr"))

      // Merges and Broadcasts
      val mergeDbRequests = b.add(Merge[DbRequest](4))
      val balanceDbQueries = b.add(Balance[DbRequest](dbParallelism))
      val mergeDbResults = b.add(Merge[PkAdd](dbParallelism))
      val broadcastFkTasks = b.add(Broadcast[FkTask](2))
      val mergeToPkRequest = b.add(Merge[PkRequest](2))
      val splitPkResults = b.add(Broadcast[PkQueryResult](2))
      val broadcastPkAdded = b.add(Broadcast[PkQueryResult](2))

      // Processing FK tasks
      baseQueryTasks ~> kickoffToDb ~> mergeDbRequests.in(0)
      broadcastFkTasks.out(0) ~> fkToDb ~> mergeDbRequests.in(1)
      splitPkResults.out(0) ~> pkExistsToDb ~> mergeDbRequests.in(2)

      // Processing DB Queries in Parallel
      mergeDbRequests.out ~> balanceDbQueries
      for (i <- 0 until dbParallelism) {
        balanceDbQueries.out(i) ~> dbToPkAdd.async ~> mergeDbResults.in(i)
      }

      // Process PkStore queries sequentially
      broadcastFkTasks.out(1) ~> fkToPkExists ~> mergeToPkRequest.in(0)
      mergeDbResults.out ~> mergeToPkRequest.in(1)

      mergeToPkRequest.out ~> pkQueryToResult ~> splitPkResults
      splitPkResults.out(1) ~> pkAdded ~> broadcastPkAdded
      broadcastPkAdded.out(0) ~> pkAddedToFkTask ~> broadcastFkTasks.in
      broadcastPkAdded.out(1) ~> pkAddedToDbCopy ~> mergeDbRequests.in(3)

      ClosedShape
    })

    dbSubsettingGraph.run
  }
}

sealed trait Task

case class InitialTask(name: String, fetchChildren: Boolean) extends Task

case class FkTask(name: String, fkPointsToPk: Boolean, fetchChildren: Boolean) extends Task

sealed trait DbRequest

sealed trait DbFetch extends DbRequest

sealed trait DbCopy extends DbRequest

case class PrepStmt(name: String) extends DbFetch

case class SqlString(name: String) extends DbFetch

case class Copy(name: String) extends DbCopy

sealed trait PkRequest

case class PkExists(name: String) extends PkRequest

case class PkAdd(name: String) extends PkRequest

case class PkQueryResult(request: PkRequest, existed: Boolean)