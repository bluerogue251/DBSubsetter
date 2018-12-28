package e2e.mixedcase

import e2e.SlickSetup
import org.scalatest.FunSuiteLike
import util.assertion.AssertionUtil

trait MixedCaseTestCases extends FunSuiteLike with MixedCaseDDL with SlickSetup with AssertionUtil {
  val testName = "mIXED_case_DB"

  import profile.api._

  override lazy val ddl = schema.create
  override lazy val dml = new MixedCaseDML(profile).dbioSeq

  test("Correct table 1 records were included") {
    assertCount(MixedCaseTable1, 1)
    assertThat(MixedCaseTable1.map(_.id).sum.result, 2)
  }

  test("Correct table 2 records were included") {
    assertCount(MixedCaseTable2, 3)
    assertThat(MixedCaseTable2.map(_.id).sum.result, 15)
    assertThat(MixedCaseTable2.map(_.mixedCaseTable1Id).sum.result, 6)
  }
}