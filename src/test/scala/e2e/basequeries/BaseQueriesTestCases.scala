package e2e.basequeries

import e2e.{AbstractEndToEndTest, SlickSetup}
import util.assertion.AssertionUtil

trait BaseQueriesTestCases extends AbstractEndToEndTest with BaseQueriesDDL with SlickSetup with AssertionUtil {
  import profile.api._

  override val ddl = schema.create
  override val dml = new BaseQueriesDML(profile).dbioSeq

  val dataSetName = "base_queries"

  test("Correct base_table records were included") {
    assertCount(BaseTable, 10)
    assertThat(BaseTable.map(_.id).sum.result, 55)
  }

  test("Correct child_table records (none) were included") {
    assertCount(ChildTable, 0)
  }
}
