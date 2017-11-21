package trw.dbsubsetter

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import trw.dbsubsetter.akkastreams.SubsettingSource
import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.SchemaInfo
import trw.dbsubsetter.workflow._

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object ApplicationAkkaStreams {
  def run(config: Config, schemaInfo: SchemaInfo, baseQueries: List[SqlStrQuery]): Unit = {
    implicit val system: ActorSystem = ActorSystem("DbSubsetter")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val ec: ExecutionContext = system.dispatcher

    SubsettingSource
      .source(config, schemaInfo, baseQueries)
      .runWith(Sink.ignore)
      .onComplete { result =>
        system.terminate()
        result match {
          case Success(_) => _
          case Failure(e) => throw e
        }
      }
  }
}
