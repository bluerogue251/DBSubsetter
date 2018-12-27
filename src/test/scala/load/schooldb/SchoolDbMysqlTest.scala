package load.schooldb

import e2e.AbstractMysqlEndToEndTest
import load.LoadTest

import scala.sys.process._

class SchoolDbMysqlTest extends AbstractMysqlEndToEndTest with SchoolDbTestCases with LoadTest {
  override val originPort = 5450
  override val programArgs = Array(
    "--schemas", "school_db,Audit",
    "--baseQuery", "school_db.Students ::: student_id % 100 = 0 ::: includeChildren",
    "--baseQuery", "school_db.standalone_table ::: id < 4 ::: includeChildren",
    "--excludeColumns", "school_db.schools(mascot)",
    "--excludeTable", "school_db.empty_table_2",
    "--preTargetBufferSize", "10000"
  )

  override def setupOriginDDL(): Unit = {
    s"./src/test/util/create_mysql_db.sh Audit $originContainerName".!!
    super.setupOriginDDL()
  }

  override def setupTargetDbs(): Unit = {
    super.setupTargetDbs()
    s"./src/test/util/create_mysql_db.sh Audit $targetSithContainerName".!!
    s"./src/test/util/sync_mysql_origin_to_target.sh Audit $originContainerName $targetSithContainerName".!!
    s"./src/test/util/create_mysql_db.sh Audit $targetAkstContainerName".!!
    s"./src/test/util/sync_mysql_origin_to_target.sh Audit $originContainerName $targetAkstContainerName".!!
  }

  override val singleThreadedRuntimeThreshold: Long = 1150000

  override val akkaStreamsRuntimeThreshold: Long = 120000
}
