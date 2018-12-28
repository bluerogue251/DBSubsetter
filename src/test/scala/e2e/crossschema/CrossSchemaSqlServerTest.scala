package e2e.crossschema

import e2e.AbstractSqlServerEndToEndTest

import scala.sys.process._

class CrossSchemaSqlServerTest extends AbstractSqlServerEndToEndTest with CrossSchemaTestCases {
  override val port = 5546
  override val programArgs = Array(
    "--schemas", "schema_1, schema_2, schema_3",
    "--baseQuery", "schema_1.schema_1_table ::: id = 2 ::: includeChildren"
  )

  override def setupOriginDDL(): Unit = {
    s"./src/test/util/create_schema_sqlserver.sh $containerName $dataSetName schema_1".!!
    s"./src/test/util/create_schema_sqlserver.sh $containerName $dataSetName schema_2".!!
    s"./src/test/util/create_schema_sqlserver.sh $containerName $dataSetName schema_3".!!
    super.setupOriginDDL()
  }
}
