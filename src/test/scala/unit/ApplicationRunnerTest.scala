package unit

import org.scalatest.FunSuite
import trw.dbsubsetter.{ApplicationRunResult, ApplicationRunner}

class ApplicationRunnerTest extends FunSuite {
  test("Woot") {
    val args: Array[String] = Array("--baseQuery", "invalid-syntax")
    val result: ApplicationRunResult = ApplicationRunner.run(args)
    assert("one" === "two")
  }
}
