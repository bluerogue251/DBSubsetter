package trw.dbsubsetter

import trw.dbsubsetter.config.{CommandLineParser, Config}
import trw.dbsubsetter.db.SchemaInfoRetrieval
import trw.dbsubsetter.workflow.BaseQueries

object Application extends App {
  val start = System.nanoTime()

  CommandLineParser.parser.parse(args, Config()) match {
    case None => System.exit(1)
    case Some(config) =>
      val schemaInfo = SchemaInfoRetrieval.getSchemaInfo(config)
      val baseQueries = BaseQueries.get(config, schemaInfo)

      if (config.isSingleThreadedDebugMode)
        ApplicationSingleThreaded.run(config, schemaInfo, baseQueries)
      else
        ApplicationAkkaStreams.run(config, schemaInfo, baseQueries)
  }

  val end = System.nanoTime()
  val tookSeconds = (end - start) / 1000000000
  println(s"DBSubsetter has completed successfully! Runtime: $tookSeconds seconds")
}
