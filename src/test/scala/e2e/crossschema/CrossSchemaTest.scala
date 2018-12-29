package e2e.crossschema

import org.scalatest.FunSuiteLike
import util.assertion.AssertionUtil
import util.slick.SlickUtil

trait CrossSchemaTest extends FunSuiteLike with AssertionUtil {
  protected val testName: String = "cross_schema"

  protected val profile: slick.jdbc.JdbcProfile

  protected def originSlick: slick.jdbc.JdbcBackend#DatabaseDef

  private val ddl: CrossSchemaDDL = new CrossSchemaDDL(profile)

  import ddl.profile.api._

  protected def prepareOriginDDL(): Unit = {
    SlickUtil.ddl(originSlick, ddl.schema.create)
  }

  protected def prepareOriginDML(): Unit = {
    SlickUtil.dml(originSlick, CrossSchemaDML.dbioSeq(ddl))
  }

  test("Correct table 1 records were included") {
    assertCount(ddl.Schema1Table, 1)
    assertThat(ddl.Schema1Table.map(_.id).sum.result, 2)
  }

  test("Correct table 2 records were included") {
    assertCount(ddl.Schema2Table, 1)
    assertThat(ddl.Schema2Table.map(_.id).sum.result, 2)
  }

  test("Correct table 3 records were included") {
    assertCount(ddl.Schema3Table, 2)
    assertThat(ddl.Schema3Table.map(_.id).sum.result, 7)
  }
}