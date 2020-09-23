package trw.dbsubsetter

import io.prometheus.client.exporter.HTTPServer
import io.prometheus.client.hotspot.DefaultExports
import trw.dbsubsetter.config.CommandLineParser
import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.BaseQueries
import trw.dbsubsetter.db.DbMetadataQueries
import trw.dbsubsetter.db.SchemaInfoRetrieval
import trw.dbsubsetter.util.Util

/**
  * Provides a very thin layer underneath the real Application object. Tests will
  * call this object rather than calling the real Application object. This is because
  * the real Application object appears to have some non-threadsafe behavior which
  * can cause tests to fail nondeterministically when executed in parallel.
  */
object ApplicationRunner {
  def run(args: Array[String]): Unit = {
    val startingTime = System.nanoTime()

    CommandLineParser.parser.parse(args, Config()) match {
      case None =>
        System.exit(1)
      case Some(config) =>
        val dbMetadata =
          DbMetadataQueries.retrieveSchemaMetadata(
            config.originDbConnectionString,
            config.schemas
          )
        val schemaInfo = SchemaInfoRetrieval.getSchemaInfo(dbMetadata, config)
        val baseQueries = BaseQueries.get(config, schemaInfo)

        val optionalMetricsEndpoint: Option[HTTPServer] =
          if (config.exposeMetrics) {
            DefaultExports.initialize()
            Some(new HTTPServer(9092, true))
          } else {
            None
          }

        if (config.singleThreadDebugMode) {
          new ApplicationSingleThreaded(config, schemaInfo, baseQueries).run()
        } else {
          ApplicationAkkaStreams.run(config, schemaInfo, baseQueries)
        }
        Util.printRuntime(startingTime)
        optionalMetricsEndpoint.foreach(_.stop())
    }
  }
}
