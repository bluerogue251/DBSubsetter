package load.schooldb

import e2e.AbstractMysqlEndToEndTest

import scala.sys.process._

class SchoolDbMysqlTest extends AbstractMysqlEndToEndTest with SchoolDbTestCases with LoadTest {
  override val originPort = 5450
  override val programArgs = Array(
    "--schemas", "school_db,Audit",
    "--baseQuery", "school_db.Students ::: student_id % 100 = 0 ::: includeChildren",
    "--baseQuery", "school_db.standalone_table ::: id < 4 ::: includeChildren",
    "--excludeColumns", "school_db.schools(mascot)"
  )

  override def setupTargetDbs(): Unit = {
    super.setupTargetDbs()
    s"./src/test/util/create_mysql_db.sh Audit $targetSingleThreadedPort".!!
    s"./src/test/util/sync_mysql_origin_to_target.sh Audit $originPort $targetSingleThreadedPort".!!
    s"./src/test/util/create_mysql_db.sh Audit $targetAkkaStreamsPort".!!
    s"./src/test/util/sync_mysql_origin_to_target.sh Audit $originPort $targetAkkaStreamsPort".!!
  }

  override def setupDDL(): Unit = {
    //    s"./src/test/util/create_mysql_db.sh `Audit` $originPort".!!
    //    super.setupDDL()
  }

  override def setupDML(): Unit = {}

  override def createOriginDb(): Unit = {
    s"docker start school_db_origin_mysql".!
  }

  override val singleThreadedRuntimeThreshold: Long = 1100000

  override val akkaStreamsRuntimeThreshold: Long = 130000
}
