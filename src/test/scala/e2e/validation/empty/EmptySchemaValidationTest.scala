package e2e.validation.empty

import org.scalatest.FunSuiteLike
import trw.dbsubsetter.DbSubsetter
import trw.dbsubsetter.DbSubsetter.{FailedValidation, SubsetCompletedSuccessfully}
import trw.dbsubsetter.config.{Config, ConfigBaseQuery, SchemaConfig}
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

  private val validSchemaConfig: SchemaConfig =
    SchemaConfig(
      schemas = Set(validationSchema),
      baseQueries = Set(validationBaseQuery)
    )

  private val validConfig: Config =
    Config(
      originDbConnectionString = dbs.origin.connectionString,
      targetDbConnectionString = dbs.targetAkkaStreams.connectionString
    )

  test("Single nonexistent schema") {
    val invalidSchemaConfig = validSchemaConfig.copy(schemas = Set(Schema("nonexistent")))
    assertErrorMessage(invalidSchemaConfig, "Specified schema not found: nonexistent")
  }

  test("Multiple nonexistent schemas") {
    val invalidSchemaConfig = validSchemaConfig.copy(schemas = Set(Schema("s1"), Schema("s2"), Schema("s3")))
    assertErrorMessage(invalidSchemaConfig, "Specified schemas not found: s1, s2, s3")
  }

  private[this] def assertErrorMessage(schemaConfig: SchemaConfig, expectedMessage: String): Unit = {
    DbSubsetter.run(schemaConfig, validConfig) match {
      case SubsetCompletedSuccessfully     => fail("Expected validation failure. Got success.")
      case FailedValidation(actualMessage) => assert(actualMessage === expectedMessage)
    }
  }
}
