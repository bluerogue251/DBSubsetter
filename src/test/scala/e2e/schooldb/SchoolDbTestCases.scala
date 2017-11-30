package e2e.schooldb

import e2e.{AbstractEndToEndTest, SlickSetup}

trait SchoolDbTestCases extends AbstractEndToEndTest with SchoolDbDDL with SlickSetup {

  import profile.api._

  override val ddl = schema.create
  override val dml = new SchoolDBDML(profile).dbioSeq

  val dataSetName = "school_db"

  test("Correct students were included") {
    assertCount(Students, 27115)
    assertThatLong(Students.map(_.studentId).sum.result, 15011156816l)
  }

  test("Correct districts were included") {
    assertCount(Districts, 99)
    assertThat(Districts.map(_.id).sum.result, 4950)
  }

  test("Purposely empty tables remained empty") {
    assertCount(EmptyTable1, 0)
    assertCount(EmptyTable2, 0)
    assertCount(EmptyTable3, 0)
    assertCount(EmptyTable4, 0)
    assertCount(EmptyTable5, 0)
  }

  test("Correct homework grades were included") {
    assertCount(HomeworkGrades, 36057)
    assertThatLong(HomeworkGrades.map(_.id).sum.result, 51948824979l)
  }

  test("Correct school_assignments were included") {
    assertCount(SchoolAssignments, 20870)
    assertThat(SchoolAssignments.map(_.schoolId).sum.result, 111467366)
    assertThatLong(SchoolAssignments.map(_.studentId).sum.result, 10304630895l)
  }

  test("Correct schools were included") {
    assertCount(Schools, 9999)
    assertThat(Schools.map(_.id).sum.result, 49995000)
  }

  test("Correct standalone_table records were included") {
    assertCount(StandaloneTable, 3)
    assertThatLong(StandaloneTable.map(_.id).sum.result, 6)
  }

  test("Correct Audit.events were included") {
    assertCount(Events, 122175)
    assertThatLong(Events.map(_.id).sum.result, 86209965622l)
  }

  test("Correct essay_assignments were included") {
    pending
  }

  test("Correct worksheet_assignments were included") {
    pending
  }

  test("Correct multiple_choice_assignments were included") {
    pending
  }
}
