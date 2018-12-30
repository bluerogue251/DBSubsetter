package e2e.crossschema

import e2e.{AbstractMysqlEndToEndTest, MysqlEndToEndTestUtil}
import util.db.{DatabaseContainer, DatabaseContainerSet, MySqlDatabase}
import util.docker.ContainerUtil

import scala.sys.process._

/*
 * This test needs to use its own docker containers rather than the one typically shared between all MySql end to end tests.
 * This is because MySql doesn't have the same concept of a "schema" inside of a "database" as other databases do. DBSubsetter
 * assumes schema names will be the same between the origin and the target DBs.
 */
class CrossSchemaTestMySql extends AbstractMysqlEndToEndTest with CrossSchemaTest {

  override protected val originPort = 5540

  override protected val programArgs = Array(
    "--schemas", "schema_1, schema_2,schema_3",
    "--baseQuery", "schema_1.schema_1_table ::: id = 2 ::: includeChildren"
  )

  override protected def startOriginContainer():Unit = {
    DatabaseContainer.startMySql(containers.origin.name, containers.origin.db.port)
  }

  override protected def startTargetContainers(): Unit = {
    DatabaseContainer.startMySql(containers.targetSingleThreaded.name, containers.targetSingleThreaded.db.port)
    DatabaseContainer.startMySql(containers.targetAkkaStreams.name, containers.targetAkkaStreams.db.port)
  }

  override protected def containers: DatabaseContainerSet[MySqlDatabase] = {
    val originContainerName = s"${testName}_origin_mysql"
    val targetSingleThreadedContainerName = s"${testName}_target_single_threaded_mysql"
    val targetAkkaStreamsContainerName = s"${testName}_target_akka_streams_mysql"

    val targetSingleThreadedPort = originPort + 1
    val targetAkkaStreamsPort = originPort + 2

    val dbName = "mysql"

    new DatabaseContainerSet[MySqlDatabase](
      MysqlEndToEndTestUtil.buildContainer(originContainerName, dbName, originPort),
      MysqlEndToEndTestUtil.buildContainer(targetSingleThreadedContainerName, dbName, targetSingleThreadedPort),
      MysqlEndToEndTestUtil.buildContainer(targetAkkaStreamsContainerName, dbName, targetAkkaStreamsPort)
    )
  }

  override def createOriginDatabase(): Unit = {
    MysqlEndToEndTestUtil.createDb(containers.origin.name, "schema_1")
    MysqlEndToEndTestUtil.createDb(containers.origin.name, "schema_2")
    MysqlEndToEndTestUtil.createDb(containers.origin.name, "schema_3")
  }

  override def prepareTargetDDL(): Unit = {
    s"./src/test/util/sync_mysql_origin_to_target.sh ${containers.origin.name} schema_1 ${containers.targetSingleThreaded.name} schema_1 ".!!
    s"./src/test/util/sync_mysql_origin_to_target.sh ${containers.origin.name} schema_2 ${containers.targetSingleThreaded.name} schema_2 ".!!
    s"./src/test/util/sync_mysql_origin_to_target.sh ${containers.origin.name} schema_3 ${containers.targetSingleThreaded.name} schema_3 ".!!
    s"./src/test/util/sync_mysql_origin_to_target.sh ${containers.origin.name} schema_1 ${containers.targetAkkaStreams.name} schema_1 ".!!
    s"./src/test/util/sync_mysql_origin_to_target.sh ${containers.origin.name} schema_2 ${containers.targetAkkaStreams.name} schema_2 ".!!
    s"./src/test/util/sync_mysql_origin_to_target.sh ${containers.origin.name} schema_3 ${containers.targetAkkaStreams.name} schema_3 ".!!
  }

  override def teardownOriginContainer(): Unit = {
    ContainerUtil.rm(containers.origin.name)
  }

  override def teardownTargetContainers(): Unit = {
    ContainerUtil.rm(containers.targetSingleThreaded.name)
    ContainerUtil.rm(containers.targetAkkaStreams.name)
  }
}
