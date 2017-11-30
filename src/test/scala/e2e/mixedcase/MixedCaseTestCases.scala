package e2e.mixedcase

import e2e.{AbstractEndToEndTest, SlickSetup}

trait MixedCaseTestCases extends AbstractEndToEndTest with MixedCaseDDL with SlickSetup {
  val dataSetName = "mIXED_case_DB"

  import profile.api._

  override lazy val ddl = schema.create
  override lazy val dml = new MixedCaseDML(profile).dbioSeq

  test("Correct table 1 records were included") {
    assertCount(MixedCaseTable1, 3)
    assertThat(MixedCaseTable1.map(_.id).sum.result, 6)
  }

  test("Correct table 2 records were included") {
    assertCount(MixedCaseTable2, 3)
    assertThat(MixedCaseTable2.map(_.id).sum.result, 16)
    assertThat(MixedCaseTable2.map(_.mixedCaseTable1Id).sum.result, 9)
  }
}