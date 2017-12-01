package e2e.crossschema

import e2e.AbstractSqlServerEndToEndTest

import scala.sys.process._

class CrossSchemaSqlServerTest extends AbstractSqlServerEndToEndTest with CrossSchemaTestCases {
  override val originPort = 5546
  override val programArgs = Array(
    "--schemas", "schema_1, schema_2, schema_3",
    "--baseQuery", "schema_1.schema_1_table ::: id = 2 ::: true"
  )

  override def setupDDL(): Unit = {
    s"./src/test/scala/e2e/crossschema/create_schemas_sqlserver.sh $containerName $dataSetName".!!
    super.setupDDL()
  }
}
