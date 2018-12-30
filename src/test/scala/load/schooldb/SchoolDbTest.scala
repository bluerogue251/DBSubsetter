package load.schooldb

import org.scalatest.FunSuiteLike
import util.assertion.AssertionUtil
import util.slick.SlickUtil

import scala.concurrent.Await
import scala.concurrent.duration.Duration

trait SchoolDbTest extends FunSuiteLike with AssertionUtil {
  protected val testName = "school_db"

  protected val profile: slick.jdbc.JdbcProfile

  protected def originSlick: slick.jdbc.JdbcBackend#DatabaseDef

  private val ddl: SchoolDbDDL = new SchoolDbDDL(profile)

  import ddl.profile.api._

  protected def prepareOriginDDL(): Unit = {
    SlickUtil.ddl(originSlick, ddl.schema.create)
  }

  protected def prepareOriginDML(): Unit = {
    val customDml = new SchoolDBDML(ddl)
    val dmlFut1 = originSlick.run(customDml.initialInserts)
    Await.result(dmlFut1, Duration.Inf)
    val dmlFut2 = originSlick.run(customDml.homeworkGradeInserts)
    Await.result(dmlFut2, Duration.Inf)
    val dmlFut3 = originSlick.run(customDml.eventInserts1)
    Await.result(dmlFut3, Duration.Inf)
    val dmlFut4 = originSlick.run(customDml.eventsInsert2)
    Await.result(dmlFut4, Duration.Inf)
    val dmlFut5 = originSlick.run(customDml.eventsInsert3)
    Await.result(dmlFut5, Duration.Inf)
    val dmlFut6 = originSlick.run(customDml.latestValedictorianCacheUpdates)
    Await.result(dmlFut6, Duration.Inf)
  }

  test("Correct students were included") {
    assertCount(ddl.Students, 35758)
    assertThatLong(ddl.Students.map(_.studentId).sum.result, 17880448387l)
  }

  test("Correct districts were included") {
    assertCount(ddl.Districts, 99)
    assertThat(ddl.Districts.map(_.id).sum.result, 4950)
  }

  test("Purposely empty tables remained empty") {
    assertCount(ddl.EmptyTable1, 0)
    assertCount(ddl.EmptyTable2, 0)
    assertCount(ddl.EmptyTable3, 0)
    assertCount(ddl.EmptyTable4, 0)
    assertCount(ddl.EmptyTable5, 0)
  }

  test("Correct homework grades were included") {
    assertCount(ddl.HomeworkGrades, 40146)
    assertThatLong(ddl.HomeworkGrades.map(_.id).sum.result, 60264681654l)
  }

  test("Correct school_assignments were included") {
    assertCount(ddl.SchoolAssignments, 26297)
    assertThat(ddl.SchoolAssignments.map(_.schoolId).sum.result, 131875362l)
    assertThatLong(ddl.SchoolAssignments.map(_.studentId).sum.result, 13150077112l)
  }

  test("Correct schools were included") {
    assertCount(ddl.Schools, 9999)
    assertThat(ddl.Schools.map(_.id).sum.result, 49995000)
  }

  test("Correct standalone_table records were included") {
    assertCount(ddl.StandaloneTable, 3)
    assertThatLong(ddl.StandaloneTable.map(_.id).sum.result, 6)
  }

  test("Correct Audit.events were included") {
    assertCount(ddl.Events, 131584)
    assertThatLong(ddl.Events.map(_.id).sum.result, 314228470029l)
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
