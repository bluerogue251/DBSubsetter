package load.schooldb

import e2e.AbstractPostgresqlEndToEndTest
import load.LoadTest

import scala.sys.process._

class SchoolDbPostgresqlTest extends AbstractPostgresqlEndToEndTest with SchoolDbTestCases with LoadTest {
  override val originPort = 5453
  override val programArgs = Array(
    "--schemas", "school_db,Audit",
    "--baseQuery", "school_db.Students ::: student_id % 100 = 0 ::: includeChildren",
    "--baseQuery", "school_db.standalone_table ::: id < 4 ::: includeChildren",
    "--excludeColumns", "school_db.schools(mascot)"
  )

  override def createOriginDb(): Unit = {
    s"docker start school_db_origin_postgres".!
  }

  override def setupDML(): Unit = {}

  override def setupDDL(): Unit = {
    //    s"psql --host 0.0.0.0 --port $originPort --user postgres $dataSetName --file ./src/test/scala/load/schooldb/create_schemas_postgresql.sql".!!
    //    super.setupDDL()
  }

  override val singleThreadedRuntimeThreshold: Long = 210000

  override val akkaStreamsRuntimeThreshold: Long = 25000
}
