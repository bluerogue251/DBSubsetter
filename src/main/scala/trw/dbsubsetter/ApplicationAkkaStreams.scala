package trw.dbsubsetter

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import trw.dbsubsetter.akkastreams.SubsettingFlow
import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.SchemaInfo
import trw.dbsubsetter.workflow._

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object ApplicationAkkaStreams {
  def run(config: Config, schemaInfo: SchemaInfo, baseQueries: Iterable[SqlStrQuery]): Unit = {
    implicit val system: ActorSystem = ActorSystem("DbSubsetter")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val ec: ExecutionContext = system.dispatcher

    val (_, future) = SubsettingFlow
      .flow(config, schemaInfo)
      .runWith(Source(baseQueries.toList), Sink.ignore)

    future.onComplete { res =>
      system.terminate()

      res match {
        case Success(_) => println("Success!")
        case Failure(e) => throw e
      }
    }
  }
}
