package e2e.validation.nonempty

import org.scalatest.FunSuiteLike
import trw.dbsubsetter.DbSubsetter.{FailedValidation, SubsetCompletedSuccessfully}
import trw.dbsubsetter.config._
import trw.dbsubsetter.db.{Schema, Table}
import trw.dbsubsetter.{DbSubsetter, db}
import util.assertion.AssertionUtil
import util.db.{Database, DatabaseSet}
import util.slick.SlickUtil

trait NonEmptySchemaValidationTest extends FunSuiteLike with AssertionUtil {
  val testName = "nonempty_schema_validation"

  protected def dbs: DatabaseSet[_ <: Database]

  protected val profile: slick.jdbc.JdbcProfile

  protected def originSlick: slick.jdbc.JdbcBackend#DatabaseDef

  private val ddl: NonEmptyDDL = new NonEmptyDDL(profile)

  import ddl.profile.api._

  protected def prepareOriginDDL(): Unit = {
    SlickUtil.ddl(originSlick, ddl.schema.create)
  }

  protected def prepareOriginDML(): Unit = {
    // No-Op
  }

  private val validSchema: db.Schema = Schema("valid_schema")

  private val validTable: db.Table = Table(validSchema, "foo")

  private val validColumn: ConfigColumn = ConfigColumn(validTable, "id")

  private val validationBaseQuery: ConfigBaseQuery =
    ConfigBaseQuery(
      table = validTable,
      whereClause = "true",
      includeChildren = true
    )

  private val validSchemaConfig: SchemaConfig =
    SchemaConfig(
      schemas = Set(validSchema),
      baseQueries = Set(validationBaseQuery)
    )

  private val validConfig: Config =
    Config(
      originDbConnectionString = dbs.origin.connectionString,
      targetDbConnectionString = dbs.targetAkkaStreams.connectionString
    )

  test("Single nonexistent schema") {
    val invalidSchemaConfig = validSchemaConfig.copy(schemas = Set(Schema("nonexistent")))
    assertErrorMessage(invalidSchemaConfig, "Specified --schemas not found in database: nonexistent")
  }

  test("Multiple nonexistent schemas") {
    val invalidSchemaConfig = validSchemaConfig.copy(schemas = Set(Schema("s1"), Schema("s2"), Schema("s3")))
    assertErrorMessage(invalidSchemaConfig, "Specified --schemas not found in database: s1, s2, s3")
  }

  test("Base Query Table Not Found") {
    val nonexistentTable = Table(validSchema, "t")
    val baseQuery = ConfigBaseQuery(nonexistentTable, "true", includeChildren = true)
    val invalidSchemaConfig = validSchemaConfig.copy(baseQueries = Set(baseQuery))
    assertErrorMessage(invalidSchemaConfig, "Table 'valid_schema.t' specified in --baseQuery not found in database")
  }

  test("Extra Primary Key Table Not Found") {
    val nonexistentTable = Table(validSchema, "nope")
    val configPk = ConfigPrimaryKey(nonexistentTable, Seq(ConfigColumn(nonexistentTable, "col_name")))
    val invalidSchemaConfig = validSchemaConfig.copy(extraPrimaryKeys = Set(configPk))
    assertErrorMessage(invalidSchemaConfig, "Table 'valid_schema.nope' specified in --primaryKey not found in database")
  }

  test("Extra Foreign Key From Table Not Found") {
    val nonexistentTable = Table(validSchema, "nope")
    val configFk = ConfigForeignKey(
      fromTable = nonexistentTable,
      fromColumns = Seq(ConfigColumn(nonexistentTable, "id")),
      toTable = validTable,
      toColumns = Seq(ConfigColumn(validTable, "id"))
    )
    val invalidSchemaConfig = validSchemaConfig.copy(extraForeignKeys = Set(configFk))
    assertErrorMessage(invalidSchemaConfig, "Table 'valid_schema.nope' specified in --foreignKey not found in database")
  }

  test("Extra Foreign Key To Table Not Found") {
    val nonexistentTable = Table(validSchema, "no")
    val configFk = ConfigForeignKey(
      fromTable = validTable,
      fromColumns = Seq(ConfigColumn(validTable, "id")),
      toTable = nonexistentTable,
      toColumns = Seq(ConfigColumn(nonexistentTable, "id"))
    )
    val invalidSchemaConfig = validSchemaConfig.copy(extraForeignKeys = Set(configFk))
    assertErrorMessage(invalidSchemaConfig, "Table 'valid_schema.no' specified in --foreignKey not found in database")
  }

  test("Exclude Table Not Found") {
    val nonexistentTable = Table(validSchema, "x")
    val invalidSchemaConfig = validSchemaConfig.copy(excludeTables = Set(nonexistentTable))
    assertErrorMessage(invalidSchemaConfig, "Table 'valid_schema.x' specified in --excludeTable not found in database")
  }

  test("Exclude Columns Table Not Found") {
    val nonexistentTable = Table(validSchema, "x")
    val invalidSchemaConfig = validSchemaConfig.copy(excludeColumns = Set(ConfigColumn(nonexistentTable, "id")))
    assertErrorMessage(
      invalidSchemaConfig,
      "Table 'valid_schema.x' specified in --excludeColumns not found in database"
    )
  }

  test("Primary Key Column Not Found") {
    val invalidColumn = ConfigColumn(validTable, "col_z")
    val configPk = ConfigPrimaryKey(validTable, Seq(invalidColumn))
    val invalidSchemaConfig = validSchemaConfig.copy(extraPrimaryKeys = Set(configPk))
    assertErrorMessage(
      invalidSchemaConfig,
      "Column 'valid_schema.foo.col_z' specified in --primaryKey not found in database"
    )
  }

  test("Foreign Key From Column Not Found") {
    val invalidColumn = ConfigColumn(validTable, "col_a")
    val configFk = ConfigForeignKey(
      fromTable = validTable,
      fromColumns = Seq(invalidColumn),
      toTable = validTable,
      toColumns = Seq(validColumn)
    )
    val invalidSchemaConfig = validSchemaConfig.copy(extraForeignKeys = Set(configFk))
    assertErrorMessage(
      invalidSchemaConfig,
      "Column 'valid_schema.foo.col_a' specified in --foreignKey not found in database"
    )
  }

  test("Foreign Key To Column Not Found") {
    val invalidColumn = ConfigColumn(validTable, "col_a")
    val configFk = ConfigForeignKey(
      fromTable = validTable,
      fromColumns = Seq(validColumn),
      toTable = validTable,
      toColumns = Seq(invalidColumn)
    )
    val invalidSchemaConfig = validSchemaConfig.copy(extraForeignKeys = Set(configFk))
    assertErrorMessage(
      invalidSchemaConfig,
      "Column 'valid_schema.foo.col_a' specified in --foreignKey not found in database"
    )
  }

  test("Exclude Column Not Found") {
    val invalidColumn = ConfigColumn(validTable, "col_b")
    val invalidSchemaConfig = validSchemaConfig.copy(excludeColumns = Set(invalidColumn))
    assertErrorMessage(
      invalidSchemaConfig,
      "Column 'valid_schema.foo.col_b' specified in --excludeColumns not found in database"
    )
  }

  test("Missing Primary Key") {
    assertErrorMessage(
      validSchemaConfig,
      """Table 'valid_schema.baz' is missing a primary key. Consider either:
        |  (a) Specifying the missing primary key with the --primaryKey command line option
        |  (b) Excluding the table in question with the --excludeTable command line option
        |  (c) Adding a primary key onto the table in your origin database
        |""".stripMargin
    )
  }

  private[this] def assertErrorMessage(schemaConfig: SchemaConfig, expectedMessage: String): Unit = {
    DbSubsetter.run(schemaConfig, validConfig) match {
      case SubsetCompletedSuccessfully     => fail("Expected validation failure. Got success.")
      case FailedValidation(actualMessage) => assert(actualMessage === expectedMessage)
    }
  }
}
