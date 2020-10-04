package unit

import org.scalatest.FunSuite
import trw.dbsubsetter.{ApplicationRunResult, ApplicationRunner, FailedToStart, Success}

/**
  * Test that the args passed in on the command line are correctly parsed and
  * validated, with the correct error messages being returned in each case.
  */
class CmdLineArgsValidationTest extends FunSuite {
  // format: off
  
  test("Arguments that can't be parsed") {
    val args: Array[String] = Array(
      "--these", "are",
      "--args", "that",
      "--can't", "be parsed"
    )
    assertErrorMessage(args, "Could not parse command line arguments.")
  }
  
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
    val args: Array[String] = buildArgs(
      "--schemas", "public",
      "--baseQuery", "public.my_table ::: true ::: includeChildren",
      "--excludeColumns", "unqualified_table(col_1, col_2)"
    )
    assertErrorMessage(args, "Invalid --excludeColumns specified: unqualified_table(col_1, col_2).")
  }

  test("Invalid --excludeColumns schema") {
    val args: Array[String] = buildArgs(
      "--schemas", "my_schema",
      "--baseQuery", "my_schema.my_table ::: true ::: includeChildren",
      "--excludeColumns", "OTHER_SCHEMA.some_table(col1, col2, col3)"
    )
    assertErrorMessage(args, "Schema 'OTHER_SCHEMA' was used in --excludeColumns but was missing from --schemas.")
  }
  
  test("Multiple --primaryKey specifications for same table") {
    val args: Array[String] = buildArgs(
      "--schemas", "my_schema",
      "--baseQuery", "my_schema.my_table ::: true ::: includeChildren",
      "--primaryKey", "my_schema.my_table(column_one)",
      "--primaryKey", "my_schema.my_table(column_two)"
    )
    assertErrorMessage(args, "--primaryKey was specified more than once for table(s): 'my_schema.my_table'.")
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
