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

  override def setupTargetDbs(): Unit = {
    super.setupTargetDbs()

    s"./util/create_mysql_db.sh schema_1 $targetSingleThreadedPort".!!
    s"./util/create_mysql_db.sh schema_2 $targetSingleThreadedPort".!!
    s"./util/create_mysql_db.sh schema_3 $targetSingleThreadedPort".!!
    s"./util/sync_mysql_origin_to_target.sh schema_1 $originPort $targetSingleThreadedPort".!!
    s"./util/sync_mysql_origin_to_target.sh schema_2 $originPort $targetSingleThreadedPort".!!
    s"./util/sync_mysql_origin_to_target.sh schema_3 $originPort $targetSingleThreadedPort".!!

    s"./util/create_mysql_db.sh schema_1 $targetAkkaStreamsPort".!!
    s"./util/create_mysql_db.sh schema_2 $targetAkkaStreamsPort".!!
    s"./util/create_mysql_db.sh schema_3 $targetAkkaStreamsPort".!!
    s"./util/sync_mysql_origin_to_target.sh schema_1 $originPort $targetAkkaStreamsPort".!!
    s"./util/sync_mysql_origin_to_target.sh schema_2 $originPort $targetAkkaStreamsPort".!!
    s"./util/sync_mysql_origin_to_target.sh schema_3 $originPort $targetAkkaStreamsPort".!!
  }
}
