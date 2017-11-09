package trw.dbsubsetter

import trw.dbsubsetter.config.{CommandLineParser, Config}
import trw.dbsubsetter.orchestration.Orchestrator

object Application extends App {
  CommandLineParser.parser.parse(args, Config()) match {
    case None => System.exit(1)
    case Some(config) => Orchestrator.doSubset(config)
  }
}
