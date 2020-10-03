package trw.dbsubsetter

object Application {

  def main(args: Array[String]): Unit = {
    ApplicationRunner.run(args) match {
      case Success =>
        System.exit(0)
      case FailedToStart(message) =>
        System.err.println("DBSubsetter failed to start. " + message)
        System.exit(1)
    }
  }

}
