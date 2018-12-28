package e2e

import util.db.{DatabaseContainer, DatabaseContainerSet, MySqlContainer, MySqlDatabase}

import scala.sys.process._

abstract class AbstractMysqlEndToEndTest extends AbstractEndToEndTest[MySqlDatabase] {
  override protected val profile = slick.jdbc.MySQLProfile

  protected def testName: String

  protected def originPort: Int

  override protected def startContainers(): DatabaseContainerSet[MySqlDatabase] = {
    val originContainerName = s"${testName}_origin_mysql"
    val targetSingleThreadedContainerName = s"${testName}_target_single_threaded_mysql"
    val targetAkkaStreamsContainerName = s"${testName}_target_akka_streams_mysql"

    val targetSingleThreadedPort = originPort + 1
    val targetAkkaStreamsPort = originPort + 2

    DatabaseContainer.startMySql(originContainerName, originPort)
    DatabaseContainer.startMySql(targetSingleThreadedContainerName, targetSingleThreadedPort)
    DatabaseContainer.startMySql(targetAkkaStreamsContainerName, targetAkkaStreamsPort)

    Thread.sleep(13000)

    val originContainer = buildContainer(originContainerName, testName, originPort)
    val targetSingleThreadedContainer = buildContainer(targetSingleThreadedContainerName, testName, targetSingleThreadedPort)
    val targetAkkaStreamsContainer = buildContainer(targetAkkaStreamsContainerName, testName, targetAkkaStreamsPort)

    new DatabaseContainerSet(originContainer, targetSingleThreadedContainer, targetAkkaStreamsContainer)
  }

  override protected def createEmptyDatabases(): Unit = {
    createMySqlDatabase(containers.origin.name)
    createMySqlDatabase(containers.targetSingleThreaded.name)
    createMySqlDatabase(containers.targetAkkaStreams.name)
  }

  override protected def prepareOriginDDL(): Unit

  override protected def prepareOriginDML(): Unit

  override protected def prepareTargetDDL(): Unit = {
    s"./src/test/util/sync_mysql_origin_to_target.sh $testName ${containers.origin.name} ${containers.targetSingleThreaded.name}".!!
    s"./src/test/util/sync_mysql_origin_to_target.sh $testName ${containers.origin.name} ${containers.targetAkkaStreams.name}".!!
  }

  override protected def postSubset(): Unit = {} // No-op

  private def buildContainer(containerName: String, dbName: String, dbPort: Int): MySqlContainer = {
    val db: MySqlDatabase = new MySqlDatabase(dbName, dbPort)
    new MySqlContainer(containerName, db)
  }

  private def createMySqlDatabase(container: String): Unit = {
    s"./src/test/util/create_mysql_db.sh $testName $container".!!
  }
}