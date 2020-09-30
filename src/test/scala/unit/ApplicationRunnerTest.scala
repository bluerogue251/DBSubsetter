package unit

import org.scalatest.FunSuite
import trw.dbsubsetter.{ApplicationRunResult, ApplicationRunner}

class ApplicationRunnerTest extends FunSuite {
  test("Woot") {
    // format: off
    val args: Array[String] = Array(
      "--schemas", "public",
      "--originDbConnStr", "whatevs",
      "--targetDbConnStr", "whatevs",
      "--baseQuery", "I ::: Am ::: Invalid"
    )
    // format: on

    val result: ApplicationRunResult = ApplicationRunner.run(args)
    assert("one" === "two")
  }
}
