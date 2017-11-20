package e2e

import java.sql.{Connection, DriverManager}

import org.scalatest.{BeforeAndAfterAll, FunSuite}
import trw.dbsubsetter.Application
import trw.dbsubsetter.db.{ColumnName, SchemaName, TableName}

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
      "--originDbParallelism", "1",
      "--targetDbParallelism", "1",
      "--singleThreadedDebugMode"
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

  test("Correct students were included") {
    assert(countTable("public", "Students") === 27115)
    assert(sumColumn("public", "Students", "student_id") === 15011156816l)
  }

  test("Correct districts were included") {
    assert(countTable("public", "districts") === 99)
    assert(sumColumn("public", "districts", "Id") === 4950)
  }

  test("Purposely empty tables remained empty") {
    assert(countTable("public", "empty_table_1") === 0)
    assert(countTable("public", "empty_table_2") === 0)
    assert(countTable("public", "empty_table_3") === 0)
    assert(countTable("public", "empty_table_4") === 0)
    assert(countTable("public", "empty_table_5") === 0)
  }

  test("Correct homework grades were included") {
    assert(countTable("public", "homework_grades") === 48076)
    assert(sumColumn("public", "homework_grades", "id") === 93303124010l)
  }

  test("Correct school_assignments were included") {
    assert(countTable("public", "school_assignments") === 20870)
    assert(sumColumn("public", "school_assignments", "school_id") === 111467366)
    assert(sumColumn("public", "school_assignments", "student_id") === 10304630895l)
  }

  test("Correct schools were included") {
    assert(countTable("public", "schools") === 9999)
    assert(sumColumn("public", "schools", "id") === 49995000)
  }

  test("Correct standalone_table records were included") {
    assert(countTable("public", "standalone_table") === 3)
    assert(sumColumn("public", "standalone_table", "id") === 6)
  }

  test("Correct Audit.events were included") {
    assert(countTable("Audit", "events") === 268265)
    assert(sumColumn("Audit", "events", "id") === 445186981712l)
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

  private def countTable(schema: SchemaName, table: TableName): Long = {
    val resultSet = targetConn.createStatement().executeQuery(s"""select count(*) from "$schema"."$table"""")
    resultSet.next()
    resultSet.getLong(1)
  }

  private def sumColumn(schema: SchemaName, table: TableName, column: ColumnName): Long = {
    val resultSet = targetConn.createStatement().executeQuery(s"""select sum("$column") from "$schema"."$table"""")
    resultSet.next()
    resultSet.getLong(1)
  }
}
