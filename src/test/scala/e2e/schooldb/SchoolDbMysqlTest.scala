package e2e.schooldb

import e2e.AbstractMysqlEndToEndTest

class SchoolDbMysqlTest extends AbstractMysqlEndToEndTest with SchoolDbTestCases {
  override val originPort = 5450
  override val programArgs = Array(
    "--schemas", "school_db,Audit",
    "--baseQuery", "school_db.Students ::: student_id % 100 = 0 ::: true",
    "--baseQuery", "school_db.standalone_table ::: id < 4 ::: true",
    "--excludeColumns", "school_db.schools(mascot)"
  )
}
