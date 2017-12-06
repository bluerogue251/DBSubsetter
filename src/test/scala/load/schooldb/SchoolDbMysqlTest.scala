package load.schooldb

import e2e.AbstractMysqlEndToEndTest

import scala.sys.process._

class SchoolDbMysqlTest extends AbstractMysqlEndToEndTest with SchoolDbTestCases {
  override val originPort = 5450
  override val programArgs = Array(
    "--schemas", "school_db,Audit",
    "--baseQuery", "school_db.Students ::: student_id % 100 = 0 ::: includeChildren",
    "--baseQuery", "school_db.standalone_table ::: id < 4 ::: includeChildren",
    "--excludeColumns", "school_db.schools(mascot)"
  )

  override def setupDDL(): Unit = {
    s"./src/test/util/create_mysql_db.sh `Audit` $originPort".!!
    super.setupDDL()
  }
}
