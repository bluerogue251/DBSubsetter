package load.physics

import e2e.{AbstractEndToEndTest, SlickSetupDDL}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

trait PhysicsTestCases extends AbstractEndToEndTest with PhysicsDDL with SlickSetupDDL {

  import profile.api._

  override val ddl = schema.create

  override def setupDML(): Unit = {
    val customDml = new PhysicsDML(profile)
    val dmlFut1 = originDb.run(customDml.initialInserts)
    Await.result(dmlFut1, Duration.Inf)
    val dmlFut2 = originDb.run(customDml.homeworkGradeInserts)
    Await.result(dmlFut2, Duration.Inf)
    val dmlFut3 = originDb.run(customDml.eventInserts1)
    Await.result(dmlFut3, Duration.Inf)
    val dmlFut4 = originDb.run(customDml.eventsInsert2)
    Await.result(dmlFut4, Duration.Inf)
    val dmlFut5 = originDb.run(customDml.eventsInsert3)
    Await.result(dmlFut5, Duration.Inf)
    val dmlFut6 = originDb.run(customDml.latestValedictorianCacheUpdates)
    Await.result(dmlFut6, Duration.Inf)
  }

  val dataSetName = "school_db"

  test("Correct students were included") {
    assertCount(Students, 35758)
    assertThatLong(Students.map(_.studentId).sum.result, 17880448387l)
  }

  test("Correct districts were included") {
    assertCount(ResearchInstitutions, 99)
    assertThat(ResearchInstitutions.map(_.id).sum.result, 4950)
  }

  test("Purposely empty tables remained empty") {
    assertCount(EmptyTable1, 0)
    assertCount(EmptyTable2, 0)
    assertCount(EmptyTable3, 0)
    assertCount(EmptyTable4, 0)
    assertCount(EmptyTable5, 0)
  }

  test("Correct homework grades were included") {
    assertCount(HomeworkGrades, 40146)
    assertThatLong(HomeworkGrades.map(_.id).sum.result, 60264681654l)
  }

  test("Correct school_assignments were included") {
    assertCount(SchoolAssignments, 26297)
    assertThat(SchoolAssignments.map(_.schoolId).sum.result, 131875362l)
    assertThatLong(SchoolAssignments.map(_.studentId).sum.result, 13150077112l)
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
    assertCount(Events, 131584)
    assertThatLong(Events.map(_.id).sum.result, 314228470029l)
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
