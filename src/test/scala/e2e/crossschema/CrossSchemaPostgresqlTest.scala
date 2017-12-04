package e2e.crossschema

import e2e.AbstractPostgresqlEndToEndTest

import scala.sys.process._

class CrossSchemaPostgresqlTest extends AbstractPostgresqlEndToEndTest with CrossSchemaTestCases {
  override val originPort = 5543
  override val programArgs = Array(
    "--schemas", "schema_1, schema_2, schema_3",
    "--baseQuery", "schema_1.schema_1_table ::: id = 2 ::: includeChildren"
  )

  override def setupDDL(): Unit = {
    s"psql --host 0.0.0.0 --port $originPort --user postgres $dataSetName --file ./src/test/scala/e2e/crossschema/create_schemas_postgresql.sql".!!
    super.setupDDL()
  }
}
