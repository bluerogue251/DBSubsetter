package e2e.crossschema

import e2e.AbstractMysqlEndToEndTest

import scala.sys.process._

class CrossSchemaTestMySql extends AbstractMysqlEndToEndTest with CrossSchemaTest {

  override protected val originPort = 5540

  override protected val programArgs = Array(
    "--schemas", "schema_1, schema_2,schema_3",
    "--baseQuery", "schema_1.schema_1_table ::: id = 2 ::: includeChildren"
  )

  override def prepareOriginDDL(): Unit = {
    s"./src/test/util/create_mysql_db.sh schema_1 ${containers.origin.name}".!!
    s"./src/test/util/create_mysql_db.sh schema_2 ${containers.origin.name}".!!
    s"./src/test/util/create_mysql_db.sh schema_3 ${containers.origin.name}".!!
    super.prepareOriginDDL()
  }

  override def prepareTargetDDL(): Unit = {
    s"./src/test/util/create_mysql_db.sh schema_1 ${containers.targetSingleThreaded.name}".!!
    s"./src/test/util/create_mysql_db.sh schema_2 ${containers.targetSingleThreaded.name}".!!
    s"./src/test/util/create_mysql_db.sh schema_3 ${containers.targetSingleThreaded.name}".!!
    s"./src/test/util/sync_mysql_origin_to_target.sh schema_1 ${containers.origin.name} ${containers.targetSingleThreaded.name}".!!
    s"./src/test/util/sync_mysql_origin_to_target.sh schema_2 ${containers.origin.name} ${containers.targetSingleThreaded.name}".!!
    s"./src/test/util/sync_mysql_origin_to_target.sh schema_3 ${containers.origin.name} ${containers.targetSingleThreaded.name}".!!

    s"./src/test/util/create_mysql_db.sh schema_1 ${containers.targetAkkaStreams.name}".!!
    s"./src/test/util/create_mysql_db.sh schema_2 ${containers.targetAkkaStreams.name}".!!
    s"./src/test/util/create_mysql_db.sh schema_3 ${containers.targetAkkaStreams.name}".!!
    s"./src/test/util/sync_mysql_origin_to_target.sh schema_1 ${containers.origin.name} ${containers.targetAkkaStreams.name}".!!
    s"./src/test/util/sync_mysql_origin_to_target.sh schema_2 ${containers.origin.name} ${containers.targetAkkaStreams.name}".!!
    s"./src/test/util/sync_mysql_origin_to_target.sh schema_3 ${containers.origin.name} ${containers.targetAkkaStreams.name}".!!
  }

}
