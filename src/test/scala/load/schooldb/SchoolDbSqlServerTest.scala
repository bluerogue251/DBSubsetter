package load.schooldb

import e2e.AbstractSqlServerEndToEndTest

import scala.sys.process._

class SchoolDbSqlServerTest extends AbstractSqlServerEndToEndTest with SchoolDbTestCases with LoadTest {
  override val originPort = 5456
  override val programArgs = Array(
    "--schemas", "school_db,Audit",
    "--baseQuery", "school_db.Students ::: student_id % 100 = 0 ::: includeChildren",
    "--baseQuery", "school_db.standalone_table ::: id < 4 ::: includeChildren",
    "--excludeColumns", "school_db.schools(mascot)"
  )

  override def createOriginDb(): Unit = {
    s"docker start school_db_sqlserver".!
  }

  override def setupDDL(): Unit = {
    //    s"./src/test/util/create_schema_sqlserver.sh $containerName $dataSetName school_db".!!
    //    s"./src/test/util/create_schema_sqlserver.sh $containerName $dataSetName Audit".!!
    //    super.setupDDL()
  }

  override def setupDML(): Unit = {}

  override val singleThreadedRuntimeThreshold: Long = 110000

  override val akkaStreamsRuntimeThreshold: Long = 25000
}
