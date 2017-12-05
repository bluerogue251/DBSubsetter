package e2e.schooldb

import e2e.AbstractSqlServerEndToEndTest

import scala.sys.process._

class SchoolDbSqlServerTest extends AbstractSqlServerEndToEndTest with SchoolDbTestCases {
  override val originPort = 5456
  override val programArgs = Array(
    "--schemas", "dbo,Audit",
    "--baseQuery", "dbo.Students ::: student_id % 100 = 0 ::: includeChildren",
    "--baseQuery", "dbo.standalone_table ::: id < 4 ::: includeChildren",
    "--excludeColumns", "dbo.schools(mascot)"
  )

  override def setupDDL(): Unit = {
    s"./src/test/scala/e2e/crossschema/create_schemas_sqlserver.sh $containerName $dataSetName".!!
    super.setupDDL()
  }
}
