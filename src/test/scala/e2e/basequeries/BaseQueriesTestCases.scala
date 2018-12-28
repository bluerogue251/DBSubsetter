package e2e.basequeries

import e2e.SlickSetup
import org.scalatest.FunSuiteLike
import util.assertion.AssertionUtil

trait BaseQueriesTestCases extends FunSuiteLike with BaseQueriesDDL with SlickSetup with AssertionUtil {
  import profile.api._

  override val ddl = schema.create
  override val dml = new BaseQueriesDML(profile).dbioSeq

  val testName = "base_queries"

  test("Correct base_table records were included") {
    assertCount(BaseTable, 10)
    assertThat(BaseTable.map(_.id).sum.result, 55)
  }

  test("Correct child_table records (none) were included") {
    assertCount(ChildTable, 0)
  }
}
