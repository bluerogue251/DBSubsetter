package e2e

import util.db._
import util.docker.ContainerUtil

import scala.sys.process._

abstract class AbstractMysqlEndToEndTest extends AbstractEndToEndTest[MySqlDatabase] {
  override protected val profile = slick.jdbc.MySQLProfile

  protected def testName: String

  protected def originPort: Int

  override protected def startOriginContainer():Unit = {
    DatabaseContainer.startMySql(containers.origin.name, containers.origin.db.port)
  }

  override protected def startTargetContainers(): Unit = {
    DatabaseContainer.startMySql(containers.targetSingleThreaded.name, containers.targetSingleThreaded.db.port)
    DatabaseContainer.startMySql(containers.targetAkkaStreams.name, containers.targetAkkaStreams.db.port)
  }

  override protected def awaitContainersReady(): Unit = Thread.sleep(13000)

  override protected def createOriginDatabase(): Unit = {
    MysqlEndToEndTestUtil.createDb(containers.origin.name, containers.origin.db.name)
  }

  override protected def createTargetDatabases(): Unit = {
    MysqlEndToEndTestUtil.createDb(containers.targetSingleThreaded.name, containers.targetSingleThreaded.db.name)
    MysqlEndToEndTestUtil.createDb(containers.targetAkkaStreams.name, containers.targetAkkaStreams.db.name)
  }

  override protected def containers: DatabaseContainerSet[MySqlDatabase] = {
    val originContainer = s"${testName}_origin_mysql"
    val targetSingleThreadedContainer = s"${testName}_target_single_threaded_mysql"
    val targetAkkaStreamsContainer = s"${testName}_target_akka_streams_mysql"
    val dbName = testName

    new DatabaseContainerSet[MySqlDatabase](
      MysqlEndToEndTestUtil.buildContainer(originContainer, dbName, originPort),
      MysqlEndToEndTestUtil.buildContainer(targetSingleThreadedContainer, dbName, originPort + 1),
      MysqlEndToEndTestUtil.buildContainer(targetAkkaStreamsContainer, dbName, originPort + 2)
    )
  }

  override protected def prepareOriginDDL(): Unit

  override protected def prepareOriginDML(): Unit

  override protected def prepareTargetDDL(): Unit = {
    s"./src/test/util/sync_mysql_origin_to_target.sh ${containers.origin.name} ${containers.origin.db.name} ${containers.targetSingleThreaded.name} ${containers.targetSingleThreaded.db.name}".!!
    s"./src/test/util/sync_mysql_origin_to_target.sh ${containers.origin.name} ${containers.origin.db.name} ${containers.targetAkkaStreams.name} ${containers.targetAkkaStreams.db.name}".!!
  }

  override protected def postSubset(): Unit = {} // No-op

  override protected def teardownOriginContainer(): Unit = {
    ContainerUtil.rm(containers.origin.name)
  }

  override protected def teardownTargetContainers(): Unit = {
    ContainerUtil.rm(containers.targetSingleThreaded.name)
    ContainerUtil.rm(containers.targetAkkaStreams.name)
  }
}

object MysqlEndToEndTestUtil {
  def buildContainer(containerName: String, dbName: String, dbPort: Int): MySqlContainer = {
    val db: MySqlDatabase = new MySqlDatabase(dbName, dbPort)
    new MySqlContainer(containerName, db)
  }

  def createDb(container: String, db: String): Unit = {
    s"./src/test/util/create_mysql_db.sh $container $db".!!
  }
}