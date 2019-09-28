package trw.dbsubsetter.e2e.basequeries

import org.scalatest.FunSuiteLike
import trw.dbsubsetter.util.assertion.AssertionUtil
import trw.dbsubsetter.util.slick.SlickUtil

trait BaseQueriesTest extends FunSuiteLike with AssertionUtil {
  protected val testName = "base_queries"

  protected val profile: slick.jdbc.JdbcProfile

  protected def originSlick: slick.jdbc.JdbcBackend#DatabaseDef

  private val ddl: BaseQueriesDDL = new BaseQueriesDDL(profile)

  import ddl.profile.api._

  protected def prepareOriginDDL(): Unit = {
    SlickUtil.ddl(originSlick, ddl.schema.create)
  }

  protected def prepareOriginDML(): Unit = {
    SlickUtil.dml(originSlick, BaseQueriesDML.dbioSeq(ddl))
  }

  test("Correct base_table records were included") {
    assertCount(ddl.BaseTable, 10)
    assertThat(ddl.BaseTable.map(_.id).sum.result, 55)
  }

  test("Correct child_table records (none) were included") {
    assertCount(ddl.ChildTable, 0)
  }
}
