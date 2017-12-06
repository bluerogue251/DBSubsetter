package load.schooldb

import e2e.AbstractSqlServerEndToEndTest

import scala.sys.process._

class SchoolDbSqlServerTest extends AbstractSqlServerEndToEndTest with SchoolDbTestCases with LoadTest {
  override val originPort = 5456
  override val programArgs = Array(
    "--schemas", "dbo,Audit",
    "--baseQuery", "dbo.Students ::: student_id % 100 = 0 ::: includeChildren",
    "--baseQuery", "dbo.standalone_table ::: id < 4 ::: includeChildren",
    "--excludeColumns", "dbo.schools(mascot)"
  )
  override val mainSchema: String = "dbo"

  override def setupDDL(): Unit = {
    s"./src/test/scala/e2e/crossschema/create_schemas_sqlserver.sh $containerName $dataSetName [Audit]".!!
    super.setupDDL()
  }

  override val singleThreadedRuntimeThreshold: Long = 130000

  override val akkaStreamsRuntimeThreshold: Long = 22000
}
