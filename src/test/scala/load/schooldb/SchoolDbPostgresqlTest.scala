package load.schooldb

import e2e.AbstractPostgresqlEndToEndTest

import scala.sys.process._

class SchoolDbPostgresqlTest extends AbstractPostgresqlEndToEndTest with SchoolDbTestCases {
  override val originPort = 5453
  override val programArgs = Array(
    "--schemas", "public,Audit",
    "--baseQuery", "public.Students ::: student_id % 100 = 0 ::: includeChildren",
    "--baseQuery", "public.standalone_table ::: id < 4 ::: includeChildren",
    "--excludeColumns", "public.schools(mascot)"
  )

  override def setupDDL(): Unit = {
    s"psql --host 0.0.0.0 --port $originPort --user postgres $dataSetName --file ./src/test/scala/e2e/schooldb/create_Audit_schema_postgresql.sql".!!
    super.setupDDL()
  }
}
