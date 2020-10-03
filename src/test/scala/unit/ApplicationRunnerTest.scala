package unit

import org.scalatest.FunSuite
import trw.dbsubsetter.{ApplicationRunResult, ApplicationRunner, FailedToStart, Success}

// format: off
class ApplicationRunnerTest extends FunSuite {
  test("Invalid --baseQuery") {
    val args: Array[String] = buildArgs(
        "--schemas", "public",
        "--baseQuery", "I ::: Am ::: Invalid"
      )
    assertErrorMessage(args, "Invalid --baseQuery specified: I ::: Am ::: Invalid.")
  }

  test("Invalid --baseQuery schema") {
    val args: Array[String] = buildArgs(
      "--schemas", "my_schema",
      "--baseQuery", "other_schema.my_table ::: true ::: includeChildren"
    )
    assertErrorMessage(args, "Schema 'other_schema' was used in --baseQuery but was missing from --schemas.")
  }
  
  test("Invalid --foreignKey") {
    val args: Array[String] = buildArgs(
      "--schemas", "public",
      "--baseQuery", "public.my_table ::: true ::: includeChildren",
      "--foreignKey", "invalid-foreign-key"
    )
    assertErrorMessage(args, "Invalid --foreignKey specified: invalid-foreign-key.")
  }

  test("Invalid --foreignKey 'from' schema") {
    val args: Array[String] = buildArgs(
      "--schemas", "to_schema",
      "--baseQuery", "to_schema.some_table ::: true ::: includeChildren",
      "--foreignKey", "from_schema.from_table(column) ::: to_schema.to_table(column)"
    )
    assertErrorMessage(args, "Schema 'from_schema' was used in --foreignKey but was missing from --schemas.")
  }

  test("Invalid --foreignKey 'to' schema") {
    val args: Array[String] = buildArgs(
      "--schemas", "from_schema",
      "--baseQuery", "from_schema.some_table ::: true ::: includeChildren",
      "--foreignKey", "from_schema.from_table(column) ::: to_schema.to_table(column)"
    )
    assertErrorMessage(args, "Schema 'to_schema' was used in --foreignKey but was missing from --schemas.")
  }
  
  test("Invalid --primaryKey") {
    val args: Array[String] = buildArgs(
      "--schemas", "public",
      "--baseQuery", "public.my_table ::: true ::: includeChildren",
      "--primaryKey", "invalid-primary-key"
    )
    assertErrorMessage(args, "Invalid --primaryKey specified: invalid-primary-key.")
  }
  
  test("Invalid --primaryKey schema") {
    val args: Array[String] = buildArgs(
      "--schemas", "my_schema",
      "--baseQuery", "my_schema.my_table ::: true ::: includeChildren",
      "--primaryKey", "some_other_schema.some_table(column_one, column_two)"
    )
    assertErrorMessage(args, "Schema 'some_other_schema' was used in --primaryKey but was missing from --schemas.")
  }
  
  test("Invalid --excludeTable") {
    val args: Array[String] = buildArgs(
      "--schemas", "public",
      "--baseQuery", "public.my_table ::: true ::: includeChildren",
      "--excludeTable", "IN.VA.LID"
    )
    assertErrorMessage(args, "Invalid --excludeTable specified: IN.VA.LID.")
  }

  test("Invalid --excludeTable schema") {
    val args: Array[String] = buildArgs(
      "--schemas", "my_schema",
      "--baseQuery", "my_schema.my_table ::: true ::: includeChildren",
      "--excludeTable", "other.some_table"
    )
    assertErrorMessage(args, "Schema 'other' was used in --excludeTable but was missing from --schemas.")
  }

  test("Invalid --excludeColumns") {
    fail()
  }

  test("Invalid --excludeColumns schema") {
    fail()
  }
  
  private[this] def buildArgs(additionalArgs: String*): Array[String] = {
    Array[String](
      "--originDbConnStr", "whatevs",
      "--targetDbConnStr", "whatevs"
    ) ++ additionalArgs
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
