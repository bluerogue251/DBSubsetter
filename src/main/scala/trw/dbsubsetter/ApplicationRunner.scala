package trw.dbsubsetter

import io.prometheus.client.exporter.HTTPServer
import io.prometheus.client.hotspot.DefaultExports
import trw.dbsubsetter.config.{CommandLineParser, Config}

import scala.concurrent.duration.Duration

/**
  * Provides a very thin layer underneath the real Application object. Tests will
  * call this object rather than calling the real Application object. This is because
  * the real Application object appears to have some non-threadsafe behavior which
  * can cause tests to fail nondeterministically when executed in parallel.
  */
object ApplicationRunner {
  def run(args: Array[String]): Unit = {

    CommandLineParser.parser.parse(args, Config()) match {
      case None =>
        System.exit(1)
      case Some(config) =>
        val metricsEndpoint: Option[HTTPServer] =
          config.metricsPort
            .map { port =>
              DefaultExports.initialize()
              new HTTPServer(port)
            }

        val took: Duration = DbSubsetter.run(config)

        metricsEndpoint.foreach(_.stop())
        println(s"DBSubsetter has completed successfully! Approximate runtime: ${took.toSeconds} seconds")
    }
  }
}
