package e2e.crossschema

import e2e.SqlServerEnabledTest

import scala.sys.process._

class CrossSchemaTestSqlServer extends SqlServerEnabledTest with CrossSchemaTest {

  override protected val programArgs = Array(
    "--schemas", "schema_1, schema_2, schema_3",
    "--baseQuery", "schema_1.schema_1_table ::: id = 2 ::: includeChildren"
  )

  override protected def prepareOriginDDL(): Unit = {
    s"./src/test/util/create_schema_sqlserver.sh ${dbs.origin.host} ${dbs.origin.name} schema_1".!!
    s"./src/test/util/create_schema_sqlserver.sh ${dbs.origin.host} ${dbs.origin.name} schema_2".!!
    s"./src/test/util/create_schema_sqlserver.sh ${dbs.origin.host} ${dbs.origin.name} schema_3".!!
    super.prepareOriginDDL()
  }
}
