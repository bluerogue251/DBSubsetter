package e2e

import java.sql.{Connection, DriverManager}

import org.scalatest.{BeforeAndAfterAll, FunSuite}
import trw.dbsubsetter.Application
import trw.dbsubsetter.db.TableName

import scala.sys.process._

class SchoolDbTest extends FunSuite with BeforeAndAfterAll {
  val targetConnString = "jdbc:postgresql://localhost:5451/school_db_target?user=postgres"
  var targetConn: Connection = _

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    //    "./util/school_db/reset_origin_db.sh".!!
    "./util/school_db/reset_target_db.sh".!!

    val args = Array(
      "--schemas", "public,Audit",
      "--originDbConnStr", "jdbc:postgresql://localhost:5450/school_db_origin?user=postgres",
      "--targetDbConnStr", targetConnString,
      "--baseQuery", "public.Students=student_id % 100 = 0",
      "--baseQuery", "public.standalone_table=id < 4",
      "--excludeColumns", "public.schools(mascot)",
      "--foreignKey", "public.homework_grades(assignment_id) ::: public.essay_assignments(id) ::: homework_grades.assignment_type='essay'",
      "--foreignKey", "public.homework_grades(assignment_id) ::: public.multiple_choice_assignments(id) ::: homework_grades.assignment_type='multiple choice'",
      "--foreignKey", "public.homework_grades(assignment_id) ::: public.worksheet_assignments(id) ::: homework_grades.assignment_type='worksheet'",
      "--originDbParallelism", "5",
      "--targetDbParallelism", "5"
      //      "--singleThreadedDebugMode"
    )
    Application.main(args)

    "./util/school_db/post_subset_target.sh".!!

    targetConn = DriverManager.getConnection(targetConnString)
    targetConn.setReadOnly(true)
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    targetConn.close()
  }

  test("Correct number of grandparents were included") {
    val resultSet = targetConn.createStatement().executeQuery("select count(*) from grandparents")
    resultSet.next()
    val grandparentCount = resultSet.getInt(1)
    assert(grandparentCount === 167)
  }

  private def countTable(table: TableName): BigInt = {
    ???
  }
}
