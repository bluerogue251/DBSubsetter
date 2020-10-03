package trw.dbsubsetter

import trw.dbsubsetter.DbSubsetter.{FailedValidation, SubsetCompletedSuccessfully}
import trw.dbsubsetter.config._

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
