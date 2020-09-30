package unit

import org.scalatest.FunSuite
import trw.dbsubsetter.{ApplicationRunResult, ApplicationRunner, FailedToStart, Success}

class ApplicationRunnerTest extends FunSuite {
  test("Base Query Regex Validation Failure") {
    // format: off
    val args: Array[String] = Array(
      "--schemas", "public",
      "--originDbConnStr", "whatevs",
      "--targetDbConnStr", "whatevs",
      "--baseQuery", "I ::: Am ::: Invalid"
    )
    // format: on

    val result: ApplicationRunResult = ApplicationRunner.run(args)

    result match {
      case Success =>
        fail("Expected invalid but actually was success.")
      case FailedToStart(message) =>
        assert(message === "Invalid --baseQuery specified: I ::: Am ::: Invalid.")
    }
  }
}
