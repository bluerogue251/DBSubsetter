package trw.dbsubsetter

import trw.dbsubsetter.config.{CommandLineParser, Config}
import trw.dbsubsetter.db.SchemaInfoRetrieval
import trw.dbsubsetter.util.Util
import trw.dbsubsetter.workflow.BaseQueries

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object Application extends App {
  val startingTime = System.nanoTime()

  CommandLineParser.parser.parse(args, Config()) match {
    case None => System.exit(1)
    case Some(config) =>
      println(config)
      System.exit(0)
      val schemaInfo = SchemaInfoRetrieval.getSchemaInfo(config)
      val baseQueries = BaseQueries.get(config, schemaInfo)

      if (config.isSingleThreadedDebugMode) {
        ApplicationSingleThreaded.run(config, schemaInfo, baseQueries)
        Util.printRuntime(startingTime)
      } else {
        val futureResult = ApplicationAkkaStreams.run(config, schemaInfo, baseQueries)
        futureResult.onComplete {
          case Success(_) =>
            Util.printRuntime(startingTime)
          case Failure(e) =>
            e.printStackTrace()
            System.exit(1)
        }
      }
  }
}
