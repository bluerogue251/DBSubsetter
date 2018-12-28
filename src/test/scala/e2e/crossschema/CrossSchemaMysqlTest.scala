package e2e.crossschema

import e2e.AbstractMysqlEndToEndTest

import scala.sys.process._

class CrossSchemaMysqlTest extends AbstractMysqlEndToEndTest with CrossSchemaTestCases {
  override val originPort = 5540
  override val programArgs = Array(
    "--schemas", "schema_1, schema_2,schema_3",
    "--baseQuery", "schema_1.schema_1_table ::: id = 2 ::: includeChildren"
  )

  override def setupOriginDDL(): Unit = {
    s"./src/test/util/create_mysql_db.sh schema_1 $originContainerName".!!
    s"./src/test/util/create_mysql_db.sh schema_2 $originContainerName".!!
    s"./src/test/util/create_mysql_db.sh schema_3 $originContainerName".!!
    super.setupOriginDDL()
  }

  override def prepareTargetDDL(): Unit = {
    super.prepareTargetDDL()

    s"./src/test/util/create_mysql_db.sh schema_1 $targetSithContainerName".!!
    s"./src/test/util/create_mysql_db.sh schema_2 $targetSithContainerName".!!
    s"./src/test/util/create_mysql_db.sh schema_3 $targetSithContainerName".!!
    s"./src/test/util/sync_mysql_origin_to_target.sh schema_1 $originContainerName $targetSithContainerName".!!
    s"./src/test/util/sync_mysql_origin_to_target.sh schema_2 $originContainerName $targetSithContainerName".!!
    s"./src/test/util/sync_mysql_origin_to_target.sh schema_3 $originContainerName $targetSithContainerName".!!

    s"./src/test/util/create_mysql_db.sh schema_1 $targetAkstContainerName".!!
    s"./src/test/util/create_mysql_db.sh schema_2 $targetAkstContainerName".!!
    s"./src/test/util/create_mysql_db.sh schema_3 $targetAkstContainerName".!!
    s"./src/test/util/sync_mysql_origin_to_target.sh schema_1 $originContainerName $targetAkstContainerName".!!
    s"./src/test/util/sync_mysql_origin_to_target.sh schema_2 $originContainerName $targetAkstContainerName".!!
    s"./src/test/util/sync_mysql_origin_to_target.sh schema_3 $originContainerName $targetAkstContainerName".!!
  }
}
