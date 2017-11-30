package e2e.schooldb

import e2e.AbstractMysqlEndToEndTest

import scala.sys.process._

class SchoolDbMysqlTest extends AbstractMysqlEndToEndTest with SchoolDbTestCases {
  override val originPort = 5456
  override val programArgs = Array(
    "--schemas", "school_db,Audit",
    "--baseQuery", "school_db.Students ::: student_id % 100 = 0 ::: true",
    "--baseQuery", "school_db.standalone_table ::: id < 4 ::: true",
    "--excludeColumns", "school_db.schools(mascot)"
  )

  override def setupDDL(): Unit = {
    s"./util/create_mysql_db.sh Audit $originPort".!!
    super.setupDDL()
  }
}
