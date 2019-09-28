package e2e.crossschema

import e2e.AbstractSqlServerEndToEndTest

import scala.sys.process._

class CrossSchemaTestSqlServer extends AbstractSqlServerEndToEndTest with CrossSchemaTest {

  override protected val programArgs = Array(
    "--schemas", "schema_1, schema_2, schema_3",
    "--baseQuery", "schema_1.schema_1_table ::: id = 2 ::: includeChildren"
  )

  override protected def prepareOriginDDL(): Unit = {
    s"./src/test/util/create_schema_sqlserver.sh ${containers.origin.name} ${containers.origin.db.name} schema_1".!!
    s"./src/test/util/create_schema_sqlserver.sh ${containers.origin.name} ${containers.origin.db.name} schema_2".!!
    s"./src/test/util/create_schema_sqlserver.sh ${containers.origin.name} ${containers.origin.db.name} schema_3".!!
    super.prepareOriginDDL()
  }
}