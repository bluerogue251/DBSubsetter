package e2e

import util.db._

import scala.sys.process._

abstract class AbstractMysqlEndToEndTest extends AbstractEndToEndTest[MySqlDatabase] {
  override protected val profile = slick.jdbc.MySQLProfile

  protected def testName: String

  protected def originPort: Int

  override protected def startOriginContainer():Unit = SharedTestContainers.mysql

  override protected def startTargetContainers(): Unit = {} // No-op (shares container with origin)

  override protected def awaitContainersReady(): Unit = Thread.sleep(13000)

  override protected def createOriginDatabase(): Unit = {
    MysqlEndToEndTestUtil.createDb(containers.origin.name, containers.origin.db.name)
  }

  override protected def createTargetDatabases(): Unit = {
    MysqlEndToEndTestUtil.createDb(containers.targetSingleThreaded.name, containers.targetSingleThreaded.db.name)
    MysqlEndToEndTestUtil.createDb(containers.targetAkkaStreams.name, containers.targetAkkaStreams.db.name)
  }

  override protected def containers: DatabaseContainerSet[MySqlDatabase] = {
    val containerName = SharedTestContainers.mysql.name
    val port = SharedTestContainers.mysql.db.port
    val originDbName = s"${testName}_origin_mysql"
    val targetSingleThreadedDbName = s"${testName}_target_single_threaded_mysql"
    val targetAkkaStreamsDbName = s"${testName}_target_akka_streams_mysql"

    new DatabaseContainerSet[MySqlDatabase](
      MysqlEndToEndTestUtil.buildContainer(containerName, originDbName, port),
      MysqlEndToEndTestUtil.buildContainer(containerName, targetSingleThreadedDbName, port),
      MysqlEndToEndTestUtil.buildContainer(containerName, targetAkkaStreamsDbName, port)
    )
  }

  override protected def prepareOriginDDL(): Unit

  override protected def prepareOriginDML(): Unit

  override protected def prepareTargetDDL(): Unit = {
    s"./src/test/util/sync_mysql_origin_to_target.sh ${containers.origin.db.name} ${containers.origin.name} ${containers.targetSingleThreaded.name}".!!
    s"./src/test/util/sync_mysql_origin_to_target.sh ${containers.origin.db.name} ${containers.origin.name} ${containers.targetAkkaStreams.name}".!!
  }

  override protected def postSubset(): Unit = {} // No-op
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