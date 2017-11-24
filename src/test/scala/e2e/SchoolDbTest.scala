package e2e

class SchoolDbTest extends AbstractEndToEndTest {
  override val dataSetName = "school_db"
  override val originPort = 5450
  override val targetPort = 5451

  override val programArgs = Array(
    "--schemas", "public,Audit",
    "--originDbConnStr", "jdbc:postgresql://localhost:5450/school_db_origin?user=postgres",
    "--targetDbConnStr", targetConnString,
    "--baseQuery", "public.Students ::: student_id % 100 = 0 ::: true",
    "--baseQuery", "public.standalone_table ::: id < 4 ::: true",
    "--excludeColumns", "public.schools(mascot)",
    "--originDbParallelism", "1",
    "--targetDbParallelism", "1",
    "--singleThreadedDebugMode"
  )

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
}
