package trw.dbsubsetter

object Application extends App {
  private val maybeConfig = CommandLineParser.parser.parse(args, Config())
  maybeConfig match {
    case None => System.exit(1)
    case Some(config) => SubsettingOrchestrator.doSubset(config)
  }
}
