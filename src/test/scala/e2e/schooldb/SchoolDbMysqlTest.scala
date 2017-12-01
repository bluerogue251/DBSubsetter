package e2e.schooldb

import org.scalatest.FunSuite

class SchoolDbMysqlTest extends FunSuite {
  //  override val originPort = 5456
  //  override val programArgs = Array(
  //    "--schemas", "school_db,Audit",
  //    "--baseQuery", "school_db.Students ::: student_id % 100 = 0 ::: true",
  //    "--baseQuery", "school_db.standalone_table ::: id < 4 ::: true",
  //    "--excludeColumns", "school_db.schools(mascot)"
  //  )

  //  override def setupDDL(): Unit = {
  //    s"./util/create_mysql_db.sh Audit $originPort".!!
  //    super.setupDDL()
  //  }

  // Pending until I rewrite the DML in vendor-agnostic slick
  // And also pending until I write a workaround to MySQL bug with wrong case of foreign key column
  test("MySQL SchoolDB Test") {
    pending
  }
}
