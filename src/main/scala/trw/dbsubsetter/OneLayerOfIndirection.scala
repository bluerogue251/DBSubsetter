package trw.dbsubsetter

import io.prometheus.client.exporter.HTTPServer
import trw.dbsubsetter.config.{CommandLineParser, Config}
import trw.dbsubsetter.db.{BaseQueries, SchemaInfoRetrieval}
import trw.dbsubsetter.util.Util

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object OneLayerOfIndirection {
  def foo(args: Array[String]): Unit = {
    val startingTime = System.nanoTime()

    CommandLineParser.parser.parse(args, Config()) match {
      case None =>
        System.exit(1)
      case Some(config) =>
        val schemaInfo = SchemaInfoRetrieval.getSchemaInfo(config)
        val baseQueries = BaseQueries.get(config, schemaInfo)

        val optionalMetricsEndpoint: Option[HTTPServer] =
          if (config.exposeMetrics) {
            Some(new HTTPServer(9092, true))
          } else {
            None
          }

        if (config.isSingleThreadedDebugMode) {
          ApplicationSingleThreaded.run(config, schemaInfo, baseQueries)
        } else {
          val futureResult = ApplicationAkkaStreams.run(config, schemaInfo, baseQueries)
          Await.ready(futureResult, Duration.Inf)
        }
        Util.printRuntime(startingTime)
        optionalMetricsEndpoint.foreach(_.stop())
    }
  }
}
