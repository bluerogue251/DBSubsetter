package e2e.validation

import org.scalatest.FunSuiteLike
import trw.dbsubsetter.config.{CmdLineBaseQuery, Config}
import trw.dbsubsetter.db.{Schema, Table}
import trw.dbsubsetter.{DbSubsetter, FailedValidation, Result, Success}
import util.assertion.AssertionUtil
import util.db.{Database, DatabaseSet}
import util.slick.SlickUtil

trait ValidationTest extends FunSuiteLike with AssertionUtil {
  val testName = "validation"

  protected val profile: slick.jdbc.JdbcProfile

  protected def originSlick: slick.jdbc.JdbcBackend#DatabaseDef

  protected def dbs: DatabaseSet[_ <: Database]

  private val ddl: ValidationDDL = new ValidationDDL(profile)

  protected def prepareOriginDDL(): Unit = {
    import ddl.profile.api._
    SlickUtil.ddl(originSlick, ddl.schema.create)
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

  test("Schema not found") {
    val invalidSchema = Schema("invalid_schema")
    val invalidConfig = validConfig.copy(schemas = Seq(invalidSchema))
    val result: Result = DbSubsetter.run(invalidConfig)
    assertErrorMessage(result, "Schema not found: invalid_schema")
  }

  private[this] def assertErrorMessage(result: Result, expectedMessage: String): Unit = {
    result match {
      case Success                         => fail("Expected validation failure. Got success.")
      case FailedValidation(actualMessage) => assert(actualMessage === expectedMessage)
    }
  }
}
