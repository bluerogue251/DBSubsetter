package e2e.mixedcase

import org.scalatest.FunSuiteLike
import util.assertion.AssertionUtil
import util.slick.SlickUtil

trait MixedCaseTest extends FunSuiteLike with AssertionUtil {
  val testName = "mIXED_case_DB"

  protected val profile: slick.jdbc.JdbcProfile

  protected def originSlick: slick.jdbc.JdbcBackend#DatabaseDef

  private val ddl: MixedCaseDDL = new MixedCaseDDL(profile)

  import ddl.profile.api._

  protected def prepareOriginDDL(): Unit = {
    SlickUtil.ddl(originSlick, ddl.schema.create)
  }

  protected def prepareOriginDML(): Unit = {
    SlickUtil.dml(originSlick, MixedCaseDML.dbioSeq(ddl))
  }

  test("Correct table 1 records were included") {
    assertCount(ddl.MixedCaseTable1, 1)
    assertThat(ddl.MixedCaseTable1.map(_.id).sum.result, 2)
  }

  test("Correct table 2 records were included") {
    assertCount(ddl.MixedCaseTable2, 3)
    assertThat(ddl.MixedCaseTable2.map(_.id).sum.result, 15)
    assertThat(ddl.MixedCaseTable2.map(_.mixedCaseTable1Id).sum.result, 6)
  }
}