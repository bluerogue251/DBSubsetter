package trw.dbsubsetter

import akka.Done
import akka.actor.{ActorRef, ActorSystem}
import akka.stream.ActorMaterializer
import trw.dbsubsetter.akkastreams.{PkStoreActor, Subsetting}
import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.{DbAccessFactory, SchemaInfo}
import trw.dbsubsetter.metrics.Metrics
import trw.dbsubsetter.workflow._
import trw.dbsubsetter.workflow.offheap.{OffHeapFkTaskQueue, OffHeapFkTaskQueueFactory}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

object ApplicationAkkaStreams {
  def run(config: Config, schemaInfo: SchemaInfo, baseQueries: Vector[BaseQuery]): Future[Done] = {
    implicit val system: ActorSystem = ActorSystem("DbSubsetter")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val ec: ExecutionContext = system.dispatcher

    val pkStore: ActorRef = system.actorOf(PkStoreActor.props(config, schemaInfo))
    val dbAccessFactory: DbAccessFactory = new DbAccessFactory(config, schemaInfo)
    val fkTaskCreationWorkflow: FkTaskCreationWorkflow = new FkTaskCreationWorkflow(schemaInfo)
    val fkTaskQueue: OffHeapFkTaskQueue = OffHeapFkTaskQueueFactory.buildOffHeapFkTaskQueue(config, schemaInfo)

    if (config.exposeMetrics) {
      Metrics.PendingTasksGauge.inc(config.baseQueries.size)
    }

    val subsettingFuture: Future[Done] =
      Subsetting
        .runnableGraph(config, schemaInfo, baseQueries, pkStore, dbAccessFactory, fkTaskCreationWorkflow, fkTaskQueue)
        .run()
        .map { success =>
          dbAccessFactory.closeAllConnections()
          system.terminate()
          success
        }

    Await.ready(subsettingFuture, Duration.Inf)
  }
}
