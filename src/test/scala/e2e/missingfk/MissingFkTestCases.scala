package e2e.missingfk

import e2e.SlickSetup
import org.scalatest.FunSuiteLike
import util.assertion.AssertionUtil

trait MissingFkTestCases extends FunSuiteLike with MissingFkDDL with SlickSetup with AssertionUtil {
  import profile.api._

  override val ddl = schema.create
  override val dml = new MissingFkDML(profile).dbioSeq

  val dataSetName = "missing_fk"

  test("Correct table_1 records were included") {
    assertCount(Table1, 1)
    assertThat(Table1.map(_.id).sum.result, 2)
  }

  test("Correct table_2 records were included") {
    assertCount(Table2, 2)
    // 1, 2
    assertThat(Table2.map(_.id).sum.result, 3)
  }

  test("Correct table_3 records were included") {
    assertCount(Table3, 2)
    // 45, 50
    assertThat(Table3.map(_.id).sum.result, 95)
  }

  test("Correct table_4 records were included") {
    assertCount(Table4, 2)
    // 2, 2
    assertThat(Table4.map(_.table1Id).sum.result, 4)
    // 45, 50
    assertThat(Table4.map(_.table3Id).sum.result, 95)
  }

  test("Correct table_5 records were included") {
    assertCount(Table5, 1)
    assertThat(Table5.map(_.id).sum.result, 99)
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
