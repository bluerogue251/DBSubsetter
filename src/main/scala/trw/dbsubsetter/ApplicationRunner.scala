package trw.dbsubsetter

import io.prometheus.client.exporter.HTTPServer
import io.prometheus.client.hotspot.DefaultExports
import trw.dbsubsetter.DbSubsetter.{DbSubsetterResult, FailedValidation, SubsetCompletedSuccessfully}
import trw.dbsubsetter.config.{CommandLineParser, Config}

/**
  * Provides a very thin layer underneath the real Application object. Tests will
  * call this object rather than calling the real Application object. This is because
  * the real Application object appears to have some non-threadsafe behavior which
  * can cause tests to fail nondeterministically when executed in parallel.
  */
object ApplicationRunner {
  def run(args: Array[String]): ApplicationRunResult = {

    CommandLineParser.parser.parse(args, Config()) match {
      case None =>
        Error(s"Could not parse command line arguments.")
      case Some(config) =>
        val metricsEndpoint: Option[HTTPServer] =
          config.metricsPort
            .map { port =>
              DefaultExports.initialize()
              new HTTPServer(port)
            }

        val result: DbSubsetterResult = DbSubsetter.run(config)

        metricsEndpoint.foreach(_.stop())

        result match {
          case SubsetCompletedSuccessfully =>
            Success
          case FailedValidation(message) =>
            Error(s"Validation error: $message.")
        }
    }
  }
}

sealed trait ApplicationRunResult
case object Success extends ApplicationRunResult
case class Error(message: String) extends ApplicationRunResult
