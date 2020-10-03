package unit

import org.scalatest.FunSuite
import trw.dbsubsetter.{ApplicationRunResult, ApplicationRunner, FailedToStart, Success}

// format: off
class ApplicationRunnerTest extends FunSuite {

  test("Invalid --baseQuery") {
    val args: Array[String] = Array(
      "--schemas", "public",
      "--originDbConnStr", "whatevs",
      "--targetDbConnStr", "whatevs",
      "--baseQuery", "I ::: Am ::: Invalid"
    )
    assertErrorMessage(args, "Invalid --baseQuery specified: I ::: Am ::: Invalid.")
  }
  
  test("Invalid --foreignKey") {
    val args: Array[String] = Array(
      "--schemas", "public",
      "--originDbConnStr", "whatevs",
      "--targetDbConnStr", "whatevs",
      "--baseQuery", "public.my_table ::: true ::: includeChildren",
      "--foreignKey", "invalid-foreign-key"
    )
    assertErrorMessage(args, "Invalid --foreignKey specified: invalid-foreign-key.")
  }
  
  test("Invalid --primaryKey") {
    val args: Array[String] = Array(
      "--schemas", "public",
      "--originDbConnStr", "whatevs",
      "--targetDbConnStr", "whatevs",
      "--baseQuery", "public.my_table ::: true ::: includeChildren",
      "--primaryKey", "invalid-primary-key"
    )
    assertErrorMessage(args, "Invalid --primaryKey specified: invalid-primary-key.")
  }
  
  test("Invalid --excludeTable") {
    val args: Array[String] = Array(
      "--schemas", "public",
      "--originDbConnStr", "whatevs",
      "--targetDbConnStr", "whatevs",
      "--baseQuery", "public.my_table ::: true ::: includeChildren",
      "--excludeTable", "IN.VA.LID"
    )
    assertErrorMessage(args, "Invalid --excludeTable specified: IN.VA.LID.")
  }

  private[this] def assertErrorMessage(args: Array[String], expectedMessage: String): Unit = {
    val result: ApplicationRunResult = ApplicationRunner.run(args)
    result match {
      case Success =>
        fail("Expected an invalid result but actual result was success.")
      case FailedToStart(message) =>
        assert(message === expectedMessage)
    }
  }
}
