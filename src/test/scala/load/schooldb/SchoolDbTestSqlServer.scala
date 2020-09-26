//package load.schooldb
//
//import e2e.SqlServerEnabledTest
//import load.LoadTest
//import util.db.SqlServerDatabase
//
//import scala.sys.process._
//
//class SchoolDbTestSqlServer extends SqlServerEnabledTest with LoadTest[SqlServerDatabase] with SchoolDbTest {
//
//  override val singleThreadedRuntimeLimitMillis: Long = 110000
//
//  override val akkaStreamsRuntimeLimitMillis: Long = 25000
//
//  override protected val programArgs = Array(
//    "--schemas", "school_db,Audit",
//    "--baseQuery", "school_db.Students ::: student_id % 100 = 0 ::: includeChildren",
//    "--baseQuery", "school_db.standalone_table ::: id < 4 ::: includeChildren",
//    "--excludeColumns", "school_db.schools(mascot)",
//    "--excludeTable", "school_db.empty_table_2"
//  )
//
//  override protected def prepareOriginDDL(): Unit = {
//    s"./src/test/util/create_schema_sqlserver.sh ${containers.origin.name} ${containers.origin.db.name} school_db".!!
//    s"./src/test/util/create_schema_sqlserver.sh ${containers.origin.name} ${containers.origin.db.name} Audit".!!
//    super.prepareOriginDDL()
//  }
//}
