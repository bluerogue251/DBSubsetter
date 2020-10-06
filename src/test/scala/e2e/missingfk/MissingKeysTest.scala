package e2e.missingfk

import org.scalatest.FunSuiteLike
import util.assertion.AssertionUtil
import util.slick.SlickUtil

trait MissingKeysTest extends FunSuiteLike with AssertionUtil {
  val testName = "missing_keys"

  protected val profile: slick.jdbc.JdbcProfile

  protected def originSlick: slick.jdbc.JdbcBackend#DatabaseDef

  private val ddl: MissingKeysDDL = new MissingKeysDDL(profile)

  import ddl.profile.api._

  protected def prepareOriginDDL(): Unit = {
    SlickUtil.ddl(originSlick, ddl.schema.create)
  }

  protected def prepareOriginDML(): Unit = {
    SlickUtil.dml(originSlick, MissingKeysDML.dbioSeq(ddl))
  }

  test("Correct table_1 records were included") {
    assertCount(ddl.Table1, 1)
    assertThat(ddl.Table1.map(_.id).sum.result, 2)
  }

  test("Correct table_2 records were included") {
    assertCount(ddl.Table2, 2)
    // 1, 2
    assertThat(ddl.Table2.map(_.id).sum.result, 3)
  }

  test("Correct table_3 records were included") {
    assertCount(ddl.Table3, 2)
    // 45, 50
    assertThat(ddl.Table3.map(_.id).sum.result, 95)
  }

  test("Correct table_4 records were included") {
    assertCount(ddl.Table4, 2)
    // 2, 2
    assertThat(ddl.Table4.map(_.table1Id).sum.result, 4)
    // 45, 50
    assertThat(ddl.Table4.map(_.table3Id).sum.result, 95)
  }

  test("Correct table_5 records were included") {
    assertCount(ddl.Table5, 1)
    assertThat(ddl.Table5.map(_.id).sum.result, 99)
  }

  test("Correct table_a records were included") {
    pending
  }

  test("Correct table_b records were included") {
    pending
  }

  test("Correct table_c records were included") {
    pending
  }

  test("Correct table_d records were included") {
    pending
  }
}
