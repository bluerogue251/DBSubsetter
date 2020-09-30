package e2e.validation.empty

import org.scalatest.FunSuiteLike
import trw.dbsubsetter.DbSubsetter
import trw.dbsubsetter.DbSubsetter.{FailedValidation, SubsetCompletedSuccessfully}
import trw.dbsubsetter.config.{Config, ConfigBaseQuery}
import trw.dbsubsetter.db.{Schema, Table}
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

  private val validationBaseQuery: ConfigBaseQuery =
    ConfigBaseQuery(
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

  test("Single nonexistent schema") {
    val invalidConfig = validConfig.copy(schemas = Seq(Schema("nonexistent")))
    assertErrorMessage(invalidConfig, "Specified schema not found: nonexistent")
  }

  test("Multiple nonexistent schemas") {
    val invalidConfig = validConfig.copy(schemas = Seq(Schema("s1"), Schema("s2"), Schema("s3")))
    assertErrorMessage(invalidConfig, "Specified schemas not found: s1, s2, s3")
  }

  private[this] def assertErrorMessage(config: Config, expectedMessage: String): Unit = {
    DbSubsetter.run(config) match {
      case SubsetCompletedSuccessfully     => fail("Expected validation failure. Got success.")
      case FailedValidation(actualMessage) => assert(actualMessage === expectedMessage)
    }
  }
}
