package trw.dbsubsetter

import io.prometheus.client.exporter.HTTPServer
import io.prometheus.client.hotspot.DefaultExports
import trw.dbsubsetter.DbSubsetter.{DbSubsetterResult, FailedValidation, SubsetCompletedSuccessfully}
import trw.dbsubsetter.config.{CommandLineArgs, CommandLineParser}

/**
  * Provides a very thin layer underneath the real Application object. Tests will
  * call this object rather than calling the real Application object. This is because
  * the real Application object appears to have some non-threadsafe behavior which
  * can cause tests to fail nondeterministically when executed in parallel.
  */
object ApplicationRunner {
  def run(args: Array[String]): ApplicationRunResult = {

    CommandLineParser.parser.parse(args, CommandLineArgs()) match {
      case None =>
        FailedToStart(s"Could not parse command line arguments.")
      case Some(commandLineConfig) =>
        val metricsEndpoint: Option[HTTPServer] =
          commandLineConfig.metricsPort
            .map { port =>
              DefaultExports.initialize()
              new HTTPServer(port)
            }

        val result: DbSubsetterResult = DbSubsetter.run(commandLineConfig)

        metricsEndpoint.foreach(_.stop())

        result match {
          case SubsetCompletedSuccessfully =>
            Success
          case FailedValidation(message) =>
            FailedToStart(message)
        }
    }
  }
}

sealed trait ApplicationRunResult
case object Success extends ApplicationRunResult
case class FailedToStart(message: String) extends ApplicationRunResult
