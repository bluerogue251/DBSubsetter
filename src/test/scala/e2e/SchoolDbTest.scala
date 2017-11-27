package e2e

class SchoolDbTest extends AbstractEndToEndTest {
  override val dataSetName = "school_db"
  override val originPort = 5450

  override val programArgs = Array(
    "--schemas", "public,Audit",
    "--baseQuery", "public.Students ::: student_id % 100 = 0 ::: true",
    "--baseQuery", "public.standalone_table ::: id < 4 ::: true",
    "--excludeColumns", "public.schools(mascot)"
  )

  test("Correct students were included") {
    assertCount("public", "Students", None, 27115)
    assertSum("public", "Students", "student_id", 15011156816l)
  }

  test("Correct districts were included") {
    assertCount("public", "districts", None, 99)
    assertSum("public", "districts", "Id", 4950)
  }

  test("Purposely empty tables remained empty") {
    assertCount("public", "empty_table_1", None, 0)
    assertCount("public", "empty_table_2", None, 0)
    assertCount("public", "empty_table_3", None, 0)
    assertCount("public", "empty_table_4", None, 0)
    assertCount("public", "empty_table_5", None, 0)
  }

  test("Correct homework grades were included") {
    assertCount("public", "homework_grades", None, 36057)
    assertSum("public", "homework_grades", "id", 51948824979l)
  }

  test("Correct school_assignments were included") {
    assertCount("public", "school_assignments", None, 20870)
    assertSum("public", "school_assignments", "school_id", 111467366)
    assertSum("public", "school_assignments", "student_id", 10304630895l)
  }

  test("Correct schools were included") {
    assertCount("public", "schools", None, 9999)
    assertSum("public", "schools", "id", 49995000)
  }

  test("Correct standalone_table records were included") {
    assertCount("public", "standalone_table", None, 3)
    assertSum("public", "standalone_table", "id", 6)
  }

  test("Correct Audit.events were included") {
    assertCount("Audit", "events", None, 122175)
    assertSum("Audit", "events", "id", 86209965622l)
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
