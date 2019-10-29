//package load.schooldb
//
//import e2e.{AbstractMysqlEndToEndTest, MysqlEndToEndTestUtil}
//import load.LoadTest
//import util.db.MySqlDatabase
//
//import scala.sys.process._
//
//class SchoolDbTestMySql extends AbstractMysqlEndToEndTest with LoadTest[MySqlDatabase] with SchoolDbTest {
//
//  override val singleThreadedRuntimeLimitMillis: Long = 1150000
//
//  override val akkaStreamsRuntimeLimitMillis: Long = 120000
//
//  override protected val programArgs = Array(
//    "--schemas", "school_db,Audit",
//    "--baseQuery", "school_db.Students ::: student_id % 100 = 0 ::: includeChildren",
//    "--baseQuery", "school_db.standalone_table ::: id < 4 ::: includeChildren",
//    "--excludeColumns", "school_db.schools(mascot)",
//    "--excludeTable", "school_db.empty_table_2",
//  )
//
//  override protected def createOriginDatabase(): Unit = {
//    super.createOriginDatabase()
//    MysqlEndToEndTestUtil.createDb(containers.origin.name, "Audit_Origin")
//  }
//
//  override protected def prepareTargetDDL(): Unit = {
//    super.prepareTargetDDL()
//    s"./src/test/util/sync_mysql_origin_to_target.sh ${containers.origin.name} Audit ${containers.targetSingleThreaded.name} Audit".!!
//    s"./src/test/util/sync_mysql_origin_to_target.sh ${containers.origin.name} Audit ${containers.targetAkkaStreams.name} Audit".!!
//  }
//}
