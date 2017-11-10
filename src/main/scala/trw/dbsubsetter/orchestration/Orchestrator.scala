package trw.dbsubsetter.orchestration

import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl.Source
import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.SchemaInfoRetrieval

import scala.concurrent.ExecutionContext

object Orchestrator {
  def doSubset(config: Config): Unit = {
    implicit val system: ActorSystem = ActorSystem("DbSubsetter")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val ec: ExecutionContext = system.dispatcher

    val schemaInfo = SchemaInfoRetrieval.getSchemaInfo(config)
    val baseQueries = BaseQueryMaker.makeBaseQueries(config, schemaInfo)
    val dbSubsettingSink = SubsettingProcessGraph.getSink(config, schemaInfo)

    Source(baseQueries).runWith(dbSubsettingSink)
  }
}