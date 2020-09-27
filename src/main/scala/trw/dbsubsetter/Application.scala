package trw.dbsubsetter

object Application extends App {
  ApplicationRunner.run(args) match {
    case Success =>
      System.out.println("Subsetting complete.")
      System.exit(0)
    case Error(message) =>
      System.err.println("Run failed with message: " + message)
      System.exit(1)
  }
}
