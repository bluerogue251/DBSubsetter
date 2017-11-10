package trw.dbsubsetter

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import trw.dbsubsetter.akkastreams.SubsettingProcessGraph
import trw.dbsubsetter.config.{CommandLineParser, Config}
import trw.dbsubsetter.db.SchemaInfoRetrieval
import trw.dbsubsetter.workflow.BaseQueries

import scala.concurrent.ExecutionContext

object Application extends App {
  CommandLineParser.parser.parse(args, Config()) match {
    case None => System.exit(1)
    case Some(config) =>
      implicit val system: ActorSystem = ActorSystem("DbSubsetter")
      implicit val materializer: ActorMaterializer = ActorMaterializer()
      implicit val ec: ExecutionContext = system.dispatcher

      val schemaInfo = SchemaInfoRetrieval.getSchemaInfo(config)
      val baseQueries = BaseQueries.get(config, schemaInfo)
      val subsettingGraph = SubsettingProcessGraph.graph(config, schemaInfo, baseQueries.toList)
      subsettingGraph.run
  }
}
