package load.schooldb

import e2e.AbstractSqlServerEndToEndTest
import load.LoadTest

class SchoolDbSqlServerTest extends AbstractSqlServerEndToEndTest with SchoolDbTestCases with LoadTest {
  override val originPort = 5456
  override val programArgs = Array(
    "--schemas", "school_db,Audit",
    "--baseQuery", "school_db.Students ::: student_id % 100 = 0 ::: includeChildren",
    "--baseQuery", "school_db.standalone_table ::: id < 4 ::: includeChildren",
    "--excludeColumns", "school_db.schools(mascot)",
    "--excludeTable", "school_db.empty_table_2",
    "--preTargetBufferSize", "10000"
  )

  override def setupOriginDb(): Unit = dockerStart("school_db_sqlserver")

  override def setupOriginDDL(): Unit = {
    //    s"./src/test/util/create_schema_sqlserver.sh $containerName $dataSetName school_db".!!
    //    s"./src/test/util/create_schema_sqlserver.sh $containerName $dataSetName Audit".!!
    //    super.setupDDL()
  }

  override def setupOriginDML(): Unit = {}

  override val singleThreadedRuntimeThreshold: Long = 110000

  override val akkaStreamsRuntimeThreshold: Long = 25000
}
