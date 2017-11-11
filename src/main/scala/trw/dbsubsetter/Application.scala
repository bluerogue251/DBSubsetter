package trw.dbsubsetter

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import trw.dbsubsetter.akkastreams.SubsettingFlow
import trw.dbsubsetter.config.{CommandLineParser, Config}
import trw.dbsubsetter.db.SchemaInfoRetrieval
import trw.dbsubsetter.workflow.BaseQueries

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object Application extends App {
  CommandLineParser.parser.parse(args, Config()) match {
    case None => System.exit(1)
    case Some(config) =>
      implicit val system: ActorSystem = ActorSystem("DbSubsetter")
      implicit val materializer: ActorMaterializer = ActorMaterializer()
      implicit val ec: ExecutionContext = system.dispatcher

      val schemaInfo = SchemaInfoRetrieval.getSchemaInfo(config)
      val baseQueries = BaseQueries.get(config, schemaInfo)
      val subsettingFlow = SubsettingFlow.flow(config, schemaInfo)
      val (_, future) = subsettingFlow.runWith(Source(baseQueries.toList), Sink.ignore)

      future.onComplete { res =>
        system.terminate()

        res match {
          case Success(_) => println("Success!")
          case Failure(e) => throw e
        }
      }
  }
}
