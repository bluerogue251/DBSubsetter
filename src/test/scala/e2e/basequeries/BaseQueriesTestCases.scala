package e2e.basequeries

import e2e.AbstractEndToEndTest

trait BaseQueriesTestCases extends AbstractEndToEndTest with BaseQueriesDDL {

  import profile.api._

  test("Correct base_table records were included") {
    assertCount(BaseTable, 10)
    assertThat(BaseTable.map(_.id).sum.result, 55)
  }

  test("Correct child_table records (none) were included") {
    assertCount(ChildTable, 0)
  }
}
