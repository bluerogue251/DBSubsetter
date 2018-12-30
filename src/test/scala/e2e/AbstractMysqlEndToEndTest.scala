package e2e

import util.db._
import util.retry.RetryUtil

abstract class AbstractMysqlEndToEndTest extends AbstractEndToEndTest[MySqlDatabase] {
  override protected val profile = slick.jdbc.MySQLProfile

  protected def testName: String

  protected def originPort: Int

  override protected def startOriginContainer():Unit = {
    DatabaseContainer.startMySql(containers.origin.name, originPort)
  }

  override protected def startTargetContainers(): Unit = {
    DatabaseContainer.startMySql(containers.targetSingleThreaded.name, containers.targetSingleThreaded.db.port)
    DatabaseContainer.startMySql(containers.targetAkkaStreams.name, containers.targetAkkaStreams.db.port)
  }

  override protected def createOriginDatabase(): Unit = {
    createMySqlDatabase(containers.origin.name, containers.origin.db.name)
  }

  override protected def createTargetDatabases(): Unit = {
    createMySqlDatabase(containers.targetSingleThreaded.name, containers.targetSingleThreaded.db.name)
    createMySqlDatabase(containers.targetAkkaStreams.name, containers.targetAkkaStreams.db.name)
    Thread.sleep(10000)
  }

  override protected def containers: DatabaseContainerSet[MySqlDatabase] = {
    val originContainerName = s"${testName}_origin_mysql"
    val targetSingleThreadedContainerName = s"${testName}_target_single_threaded_mysql"
    val targetAkkaStreamsContainerName = s"${testName}_target_akka_streams_mysql"
    val targetSingleThreadedPort = originPort + 1
    val targetAkkaStreamsPort = originPort + 2

    new DatabaseContainerSet[MySqlDatabase](
      buildContainer(originContainerName, testName, originPort),
      buildContainer(targetSingleThreadedContainerName, testName, targetSingleThreadedPort),
      buildContainer(targetAkkaStreamsContainerName, testName, targetAkkaStreamsPort)
    )
  }

  override protected def prepareOriginDDL(): Unit

  override protected def prepareOriginDML(): Unit

  override protected def prepareTargetDDL(): Unit = {
    def sync(targetContainer: String): Unit = {
      val cmd = s"./src/test/util/sync_mysql_origin_to_target.sh ${containers.origin.db.name} ${containers.origin.name} $targetContainer"
      RetryUtil.withRetry(cmd)
    }
    sync(containers.targetSingleThreaded.name)
    sync(containers.targetAkkaStreams.name)
  }

  override protected def postSubset(): Unit = {} // No-op

  private def buildContainer(containerName: String, dbName: String, dbPort: Int): MySqlContainer = {
    val db: MySqlDatabase = new MySqlDatabase(dbName, dbPort)
    new MySqlContainer(containerName, db)
  }

  private def createMySqlDatabase(container: String, db: String): Unit = {
    val command = s"./src/test/util/create_mysql_db.sh $db $container"
    RetryUtil.withRetry(command)
  }
}