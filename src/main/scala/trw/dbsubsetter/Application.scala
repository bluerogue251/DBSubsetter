package trw.dbsubsetter

object Application extends App {
  ApplicationRunner.run(args) match {
    case Success =>
      System.exit(0)
    case Error(message) =>
      System.err.println("Run failed. " + message)
      System.exit(1)
  }
}
