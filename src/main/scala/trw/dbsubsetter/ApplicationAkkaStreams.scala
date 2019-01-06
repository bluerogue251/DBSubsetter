package trw.dbsubsetter

import akka.Done
import akka.actor.{ActorRef, ActorSystem}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import trw.dbsubsetter.akkastreams.{PkStoreActor, Subsetting}
import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.{DbAccessFactory, SchemaInfo}
import trw.dbsubsetter.metrics.Metrics
import trw.dbsubsetter.workflow._
import trw.dbsubsetter.workflow.offheap.{OffHeapFkTaskQueue, OffHeapFkTaskQueueFactory}

import scala.concurrent.{ExecutionContext, Future}

object ApplicationAkkaStreams {
  def run(config: Config, schemaInfo: SchemaInfo, baseQueries: Vector[BaseQuery]): Future[Done] = {
    implicit val system: ActorSystem = ActorSystem("DbSubsetter")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val ec: ExecutionContext = system.dispatcher

    val pkStore: ActorRef = system.actorOf(PkStoreActor.props(schemaInfo))
    val dbAccessFactory: DbAccessFactory = new DbAccessFactory(config, schemaInfo)
    val fkTaskCreationWorkflow: FkTaskCreationWorkflow = new FkTaskCreationWorkflow(schemaInfo)
    val fkTaskQueue: OffHeapFkTaskQueue = OffHeapFkTaskQueueFactory.buildOffHeapFkTaskQueue(config, schemaInfo)

    if (config.exposeMetrics) {
      Metrics.PendingTasksGauge.inc(config.baseQueries.size)
    }

    Subsetting
      .source(config, schemaInfo, baseQueries, pkStore, dbAccessFactory, fkTaskCreationWorkflow, fkTaskQueue)
      .runWith(Sink.ignore)
      .map { success =>
        dbAccessFactory.closeAllConnections()
        system.terminate()
        success
      }
  }
}