package e2e.crossschema

import e2e.AbstractMysqlEndToEndTest

import scala.sys.process._

class CrossSchemaMysqlTest extends AbstractMysqlEndToEndTest with CrossSchemaTestCases {
  override val originPort = 5540
  override val programArgs = Array(
    "--schemas", "schema_1, schema_2,schema_3",
    "--baseQuery", "schema_1.schema_1_table ::: id = 2 ::: true"
  )

  override def setupDDL(): Unit = {
    s"./util/create_mysql_db.sh schema_1 $originPort".!!
    s"./util/create_mysql_db.sh schema_2 $originPort".!!
    s"./util/create_mysql_db.sh schema_3 $originPort".!!
    super.setupDDL()
  }
}
