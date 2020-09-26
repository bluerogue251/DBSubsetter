package trw.dbsubsetter.akkastreams

import java.util.concurrent.TimeUnit

import akka.Done
import akka.actor.ActorRef
import akka.pattern.ask
import akka.stream.ClosedShape
import akka.stream.scaladsl.GraphDSL.Implicits._
import akka.stream.scaladsl.{Balance, Broadcast, Flow, GraphDSL, Merge, Partition, RunnableGraph, Source}
import akka.util.Timeout
import trw.dbsubsetter.config.Config
import trw.dbsubsetter.datacopyqueue.DataCopyQueue
import trw.dbsubsetter.db.{DbAccessFactory, SchemaInfo}
import trw.dbsubsetter.fktaskqueue.ForeignKeyTaskQueue
import trw.dbsubsetter.workflow.{AlreadySeen, _}

import scala.concurrent.{ExecutionContext, Future}

object KeyQueryGraphFactory {

  def build(
      config: Config,
      schemaInfo: SchemaInfo,
      baseQueries: Seq[BaseQuery],
      pkStore: ActorRef,
      dbAccessFactory: DbAccessFactory,
      fkTaskCreationWorkflow: FkTaskCreationWorkflow,
      fkTaskQueue: ForeignKeyTaskQueue,
      dataCopyQueue: DataCopyQueue
  )(implicit ec: ExecutionContext): RunnableGraph[Future[Done]] = RunnableGraph.fromGraph(
    GraphDSL.create(BufferFactory.dataCopyBufferSink(dataCopyQueue)) { implicit b => dataCopyBufferSink =>
      // Infrastructure: Timeouts, Merges, Balances, Partitions, Broadcasts
      implicit val askTimeout: Timeout =
        Timeout(48, TimeUnit.HOURS) // For `mapAsyncUnordered`. The need for this timeout may be a code smell.
      val mergeOriginDbRequests = b.add(Merge[OriginDbRequest](3))
      val balanceOriginDb =
        b.add(Balance[OriginDbRequest](config.keyCalculationDbConnectionCount, waitForAllDownstreams = true))
      val mergeOriginDbResults = b.add(Merge[OriginDbResult](config.keyCalculationDbConnectionCount))
      val partitionFkTasks = b.add(
        Partition[ForeignKeyTask](
          2,
          {
            case t: FetchParentTask => if (FkTaskPreCheck.shouldPrecheck(t)) 1 else 0
            case _                  => 0
          }
        )
      )
      // TODO try to turn this broadcast into a typesafe Partition stage with two output ports, each output port with a different type
      val broadcastPkExistResult = b.add(Broadcast[PkQueryResult](2))
      val broadcastPksAdded = b.add(Broadcast[PksAdded](2))
      val fkTaskBufferFlow = b.add(BufferFactory.fkTaskBuffer(fkTaskQueue).async)
      val mergeToOutstandingTaskCounter = b.add(Merge[IndexedSeq[ForeignKeyTask]](2))

      // Start everything off
      Source(baseQueries.toVector) ~>
        mergeOriginDbRequests

      // Process Origin DB Queries in Parallel
      mergeOriginDbRequests.out ~> balanceOriginDb
      for (_ <- 0 until config.keyCalculationDbConnectionCount) {
        balanceOriginDb ~> OriginDb.query(config, schemaInfo, dbAccessFactory).async ~> mergeOriginDbResults
      }

      // TODO try to understand if it's really necessary for pkStore to be in an actor... given that we should
      //   only be writing to it from one place, are we really worried about threadsafety?
      mergeOriginDbResults ~>
        Flow[OriginDbResult].mapAsyncUnordered(10)(dbResult => (pkStore ? dbResult).mapTo[PksAdded]) ~>
        broadcastPksAdded

      broadcastPksAdded ~>
        FkTaskCreation.flow(fkTaskCreationWorkflow) ~>
        mergeToOutstandingTaskCounter

      mergeToOutstandingTaskCounter ~>
        TaskCountCircuitBreaker.statefulCounter(baseQueries.size) ~>
        Flow[IndexedSeq[ForeignKeyTask]].mapConcat(_.to[collection.immutable.Iterable]) ~>
        fkTaskBufferFlow

      // Do we need a small in-memory buffer so the many targetDbs never wait on the single chronicle queue?
      broadcastPksAdded ~>
        dataCopyBufferSink

      // FkTasks ~> cannotBePrechecked       ~>        OriginDbRequest
      // FkTasks ~> canBePrechecked ~> PkStoreQuery ~> OriginDbRequest
      //                                            ~> DuplicateTask
      fkTaskBufferFlow ~>
        partitionFkTasks

      partitionFkTasks.out(0) ~>
        mergeOriginDbRequests

      partitionFkTasks.out(1) ~>
        Flow[ForeignKeyTask].mapAsyncUnordered(10)(req => (pkStore ? req).mapTo[PkQueryResult]) ~>
        broadcastPkExistResult

      broadcastPkExistResult ~>
        Flow[PkQueryResult].collect { case NotAlreadySeen(fkTask) => fkTask } ~>
        mergeOriginDbRequests

      broadcastPkExistResult ~>
        Flow[PkQueryResult].collect { case AlreadySeen => IndexedSeq.empty[ForeignKeyTask] } ~>
        mergeToOutstandingTaskCounter

    ClosedShape
    }
  )

}
