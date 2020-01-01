package trw.dbsubsetter

import akka.Done
import akka.actor.{ActorRef, ActorSystem, PoisonPill}
import akka.stream.ActorMaterializer
import trw.dbsubsetter.akkastreams.{DataCopyGraphFactory, KeyQueryGraphFactory, PkStoreActor}
import trw.dbsubsetter.config.Config
import trw.dbsubsetter.datacopyqueue.{DataCopyQueue, DataCopyQueueFactory}
import trw.dbsubsetter.db.{DbAccessFactory, SchemaInfo}
import trw.dbsubsetter.fktaskqueue.{ForeignKeyTaskQueue, ForeignKeyTaskQueueFactory}
import trw.dbsubsetter.workflow._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

object ApplicationAkkaStreams {
  def run(config: Config, schemaInfo: SchemaInfo, baseQueries: Vector[BaseQuery]): Unit = {
    implicit val system: ActorSystem = ActorSystem("DbSubsetter")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val ec: ExecutionContext = system.dispatcher

    val dbAccessFactory: DbAccessFactory = new DbAccessFactory(config, schemaInfo)
    val dataCopyQueue: DataCopyQueue = DataCopyQueueFactory.buildDataCopyQueue(config, schemaInfo)

    // Encapsulate in method so as to encourage JVM to garbage-collect the Primary Key Store as early as possible
    def runKeyQueryPhase(): Unit = {
      val pkStore: ActorRef = system.actorOf(PkStoreActor.props(config, schemaInfo))
      val fkTaskCreationWorkflow: FkTaskCreationWorkflow = new FkTaskCreationWorkflow(schemaInfo)
      val fkTaskQueue: ForeignKeyTaskQueue = ForeignKeyTaskQueueFactory.build(config, schemaInfo)

      val keyQueryPhase: Future[Done] =
        KeyQueryGraphFactory
          .build(config, schemaInfo, baseQueries, pkStore, dbAccessFactory, fkTaskCreationWorkflow, fkTaskQueue, dataCopyQueue)
          .run()

      // Wait for the key query phase to complete. Use `result` rather than `ready` to ensure an exception is thrown on failure.
      Await.result(keyQueryPhase, Duration.Inf)

      // Explicitly remove the actor from the actor system to allow for JVM Garbage Collection
      pkStore.tell(PoisonPill, ActorRef.noSender)
    }

    // Encapsulate in method for consistency with runKeyQueryPhase()
    def runDataCopyPhase(): Unit = {
      val dataCopyPhase: Future[Done] =
        DataCopyGraphFactory
          .build(config, schemaInfo, dbAccessFactory, dataCopyQueue)
          .run()

      // Wait for the data copy phase to complete. Use `result` rather than `ready` to ensure an exception is thrown on failure.
      Await.result(dataCopyPhase, Duration.Inf)
    }

    runKeyQueryPhase()
    runDataCopyPhase()

    // Clean up after successful subsetting run
    dbAccessFactory.closeAllConnections()
    system.terminate()
  }
}
