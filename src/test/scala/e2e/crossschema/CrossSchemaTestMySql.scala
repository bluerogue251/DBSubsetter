package e2e.crossschema

import e2e.{AbstractMysqlEndToEndTest, MysqlEndToEndTestUtil}

import scala.sys.process._

class CrossSchemaTestMySql extends AbstractMysqlEndToEndTest with CrossSchemaTest {

  override protected val originPort = 5540

  override protected val programArgs = Array(
    "--schemas", "schema_1, schema_2,schema_3",
    "--baseQuery", "schema_1.schema_1_table ::: id = 2 ::: includeChildren"
  )

  override def createOriginDatabase(): Unit = {
    MysqlEndToEndTestUtil.createDb(containers.origin.name, "schema_1")
    MysqlEndToEndTestUtil.createDb(containers.origin.name, "schema_2")
    MysqlEndToEndTestUtil.createDb(containers.origin.name, "schema_3")
  }

  override def prepareTargetDDL(): Unit = {
    MysqlEndToEndTestUtil.createDb(containers.targetSingleThreaded.name, "schema_1")
    MysqlEndToEndTestUtil.createDb(containers.targetSingleThreaded.name, "schema_2")
    MysqlEndToEndTestUtil.createDb(containers.targetSingleThreaded.name, "schema_3")
    s"./src/test/util/sync_mysql_origin_to_target.sh ${containers.origin.name} schema_1 ${containers.targetSingleThreaded.name} schema_1 ".!!
    s"./src/test/util/sync_mysql_origin_to_target.sh ${containers.origin.name} schema_2 ${containers.targetSingleThreaded.name} schema_2 ".!!
    s"./src/test/util/sync_mysql_origin_to_target.sh ${containers.origin.name} schema_3 ${containers.targetSingleThreaded.name} schema_3 ".!!

    MysqlEndToEndTestUtil.createDb(containers.targetAkkaStreams.name, "schema_1")
    MysqlEndToEndTestUtil.createDb(containers.targetAkkaStreams.name, "schema_2")
    MysqlEndToEndTestUtil.createDb(containers.targetAkkaStreams.name, "schema_3")
    s"./src/test/util/sync_mysql_origin_to_target.sh ${containers.origin.name} schema_1 ${containers.targetAkkaStreams.name} schema_1 ".!!
    s"./src/test/util/sync_mysql_origin_to_target.sh ${containers.origin.name} schema_2 ${containers.targetAkkaStreams.name} schema_2 ".!!
    s"./src/test/util/sync_mysql_origin_to_target.sh ${containers.origin.name} schema_3 ${containers.targetAkkaStreams.name} schema_3 ".!!
  }
}
