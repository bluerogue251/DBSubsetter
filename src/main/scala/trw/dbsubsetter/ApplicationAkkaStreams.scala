package trw.dbsubsetter

import akka.Done
import akka.actor.{ActorRef, ActorSystem}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import trw.dbsubsetter.akkastreams.{PkStore, Subsetting}
import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.SchemaInfo
import trw.dbsubsetter.workflow._

import scala.concurrent.{ExecutionContext, Future}

object ApplicationAkkaStreams {
  def run(config: Config, schemaInfo: SchemaInfo, baseQueries: List[SqlStrQuery]): Future[Done] = {
    implicit val system: ActorSystem = ActorSystem("DbSubsetter")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val ec: ExecutionContext = system.dispatcher
    val pkStore: ActorRef = system.actorOf(PkStore.props(schemaInfo))

    Subsetting
      .source(config, schemaInfo, baseQueries, pkStore)
      .runWith(Sink.ignore)
      .map { success =>
        system.terminate()
        success
      }
  }
}