package load.schooldb

import e2e.AbstractMysqlEndToEndTest
import load.LoadTest
import util.db.MySqlDatabase

import scala.sys.process._

class SchoolDbTestMySql extends AbstractMysqlEndToEndTest with LoadTest[MySqlDatabase] with SchoolDbTest {

  override val singleThreadedRuntimeLimitMillis: Long = 1150000

  override val akkaStreamsRuntimeLimitMillis: Long = 120000

  override protected val originPort = 5450

  override protected val programArgs = Array(
    "--schemas", "school_db,Audit",
    "--baseQuery", "school_db.Students ::: student_id % 100 = 0 ::: includeChildren",
    "--baseQuery", "school_db.standalone_table ::: id < 4 ::: includeChildren",
    "--excludeColumns", "school_db.schools(mascot)",
    "--excludeTable", "school_db.empty_table_2",
    "--preTargetBufferSize", "10000"
  )

  override protected def createOriginDatabase(): Unit = {
    super.createOriginDatabase()
    s"./src/test/util/create_mysql_db.sh Audit ${containers.origin.name}".!!
  }

  override protected def prepareTargetDDL(): Unit = {
    super.prepareTargetDDL()
    s"./src/test/util/sync_mysql_origin_to_target.sh Audit ${containers.origin.name} ${containers.targetSingleThreaded.name}".!!
    s"./src/test/util/sync_mysql_origin_to_target.sh Audit ${containers.origin.name} ${containers.targetAkkaStreams.name}".!!
  }
}
