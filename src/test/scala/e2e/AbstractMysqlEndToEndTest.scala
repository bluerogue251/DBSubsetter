package e2e

import util.db.{DatabaseContainer, MySqlDatabase}

import scala.sys.process._

abstract class AbstractMysqlEndToEndTest extends AbstractEndToEndTest {
  override val profile = slick.jdbc.MySQLProfile

  def originPort: Int

  def testName: String

  override protected def createContainers(): Unit = {
    val originContainerName = s"${testName}_origin_mysql"
    val targetSingleThreadedContainerName = s"${testName}_target_single_threaded_mysql"
    val targetAkkaStreamsContainerName = s"${testName}_target_akka_streams_mysql"

    val targetSingleThreadedPort = originPort + 1
    val targetAkkaStreamsPort = originPort + 2

    DatabaseContainer.startMySql(originContainerName, originPort)
    DatabaseContainer.startMySql(targetSingleThreadedContainerName, targetSingleThreadedPort)
    DatabaseContainer.startMySql(targetAkkaStreamsContainerName, targetAkkaStreamsPort)

    Thread.sleep(13000)
  }

  override def prepareOriginDb(): Unit = {
    createMySqlDatabase(originContainerName)
  }

  override def prepareTargetDbs(): Unit = {
    createMySqlDatabase(targetSithContainerName)
    createMySqlDatabase(targetAkstContainerName)
    s"./src/test/util/sync_mysql_origin_to_target.sh $testName $originContainerName $targetSithContainerName".!!
    s"./src/test/util/sync_mysql_origin_to_target.sh $testName $originContainerName $targetAkstContainerName".!!
  }

  override def postSubset(): Unit = {} // No-op

  private def container(containerName: String, dbName: String, dbPort: Int): DatabaseContainer[MySqlDatabase] {
    return DatabaseContainer
  }

  private def createMySqlDatabase(container: String): Unit = s"./src/test/util/create_mysql_db.sh $testName $container".!!
}
