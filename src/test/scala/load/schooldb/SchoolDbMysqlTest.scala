package load.schooldb

import e2e.AbstractMysqlEndToEndTest
import load.LoadTest

import scala.sys.process._

class SchoolDbMysqlTest extends AbstractMysqlEndToEndTest with SchoolDbTestCases with LoadTest {
  override protected val originPort = 5450

  override protected val programArgs = Array(
    "--schemas", "school_db,Audit",
    "--baseQuery", "school_db.Students ::: student_id % 100 = 0 ::: includeChildren",
    "--baseQuery", "school_db.standalone_table ::: id < 4 ::: includeChildren",
    "--excludeColumns", "school_db.schools(mascot)",
    "--excludeTable", "school_db.empty_table_2",
    "--preTargetBufferSize", "10000"
  )

  override protected def prepareOriginDDL(): Unit = {
    super.prepareOriginDDL()
    s"./src/test/util/create_mysql_db.sh Audit ${containers.origin.name}".!!
  }

  override protected def prepareTargetDDL(): Unit = {
    super.prepareTargetDDL()
    s"./src/test/util/create_mysql_db.sh Audit ${containers.targetSingleThreaded.name}".!!
    s"./src/test/util/sync_mysql_origin_to_target.sh Audit ${containers.origin.name} ${containers.targetSingleThreaded.name}".!!
    s"./src/test/util/create_mysql_db.sh Audit ${containers.targetAkkaStreams.name}".!!
    s"./src/test/util/sync_mysql_origin_to_target.sh Audit ${containers.origin.name} ${containers.targetAkkaStreams.name}".!!
  }

  override val singleThreadedRuntimeThreshold: Long = 1150000

  override val akkaStreamsRuntimeThreshold: Long = 120000
}
