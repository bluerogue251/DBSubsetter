package e2e.validation.empty

import org.scalatest.FunSuiteLike
import trw.dbsubsetter.config.{CmdLineBaseQuery, Config}
import trw.dbsubsetter.db.{Schema, Table}
import trw.dbsubsetter.{DbSubsetter, FailedValidation, Success}
import util.assertion.AssertionUtil
import util.db.{Database, DatabaseSet}

trait EmptySchemaValidationTest extends FunSuiteLike with AssertionUtil {
  val testName = "empty_schema_validation"

  protected val profile: slick.jdbc.JdbcProfile

  protected def originSlick: slick.jdbc.JdbcBackend#DatabaseDef

  protected def dbs: DatabaseSet[_ <: Database]

  protected def prepareOriginDDL(): Unit = {
    // No-Op
  }

  protected def prepareOriginDML(): Unit = {
    // No-Op
  }

  private val validationSchema: Schema = Schema("validation_schema")

  private val validationTable: Table = Table(validationSchema, "validation_table")

  private val validationBaseQuery: CmdLineBaseQuery =
    CmdLineBaseQuery(
      table = validationTable,
      whereClause = "true",
      includeChildren = true
    )

  private val validConfig: Config =
    Config(
      schemas = Seq(validationSchema),
      originDbConnectionString = dbs.origin.connectionString,
      targetDbConnectionString = dbs.targetAkkaStreams.connectionString,
      baseQueries = Seq(validationBaseQuery)
    )

  test("Nonexistent schema") {
    val nonexistentSchema = Schema("nonexistent_schema")
    val invalidConfig = validConfig.copy(schemas = Seq(nonexistentSchema))
    assertErrorMessage(invalidConfig, "Schema not found: nonexistent_schema")
  }

  // TODO add a nicer error messages for totally uncaught exceptions (bugs)

  private[this] def assertErrorMessage(config: Config, expectedMessage: String): Unit = {
    DbSubsetter.run(config) match {
      case Success                         => fail("Expected validation failure. Got success.")
      case FailedValidation(actualMessage) => assert(actualMessage === expectedMessage)
    }
  }
}
