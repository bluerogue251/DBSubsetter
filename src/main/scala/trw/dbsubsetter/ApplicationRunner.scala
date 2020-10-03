package trw.dbsubsetter

import trw.dbsubsetter.DbSubsetter.{FailedValidation, SubsetCompletedSuccessfully}
import trw.dbsubsetter.config._

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
      case Some(commandLineArgs) =>
        ConfigExtractor.extractConfig(commandLineArgs) match {
          case Invalid(invalidInputType) =>
            FailedToStart(InvalidInputMessaging.toErrorMessage(invalidInputType))
          case Valid(schemaConfig, config) =>
            DbSubsetter.run(schemaConfig, config) match {
              case FailedValidation(message) =>
                FailedToStart(message)
              case SubsetCompletedSuccessfully =>
                Success
            }
        }
    }
  }
}

sealed trait ApplicationRunResult
case object Success extends ApplicationRunResult
case class FailedToStart(message: String) extends ApplicationRunResult
