package load.schooldb

import e2e.AbstractPostgresqlEndToEndTest
import load.LoadTest

class SchoolDbPostgresqlTest extends AbstractPostgresqlEndToEndTest with SchoolDbTestCases with LoadTest {
  override val originPort = 5453
  override val programArgs = Array(
    "--schemas", "school_db,Audit",
    "--baseQuery", "school_db.Students ::: student_id % 100 = 0 ::: includeChildren",
    "--baseQuery", "school_db.standalone_table ::: id < 4 ::: includeChildren",
    "--excludeColumns", "school_db.schools(mascot)",
    "--excludeTable", "school_db.empty_table_2",
    "--preTargetBufferSize", "10000"
  )

  override def setupOriginDb(): Unit = dockerStart("school_db_origin_postgres")

  override def setupDML(): Unit = {}

  override def setupDDL(): Unit = {
    //    s"psql --host 0.0.0.0 --port $originPort --user postgres $dataSetName --file ./src/test/scala/load/schooldb/create_schemas_postgresql.sql".!!
    //    super.setupDDL()
  }

  override val singleThreadedRuntimeThreshold: Long = 220000

  override val akkaStreamsRuntimeThreshold: Long = 25000
}
