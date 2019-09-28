package trw.dbsubsetter.e2e

import trw.dbsubsetter.util.db._

import scala.sys.process._

abstract class AbstractMysqlEndToEndTest extends AbstractEndToEndTest[MySqlDatabase] {
  override protected val profile = slick.jdbc.MySQLProfile

  protected def testName: String

  override protected def startOriginContainer():Unit = SharedTestContainers.mysqlOrigin

  override protected def startTargetContainers(): Unit = {
    SharedTestContainers.mysqlTargetSingleThreaded
    SharedTestContainers.mysqlTargetAkkaStreams
  }

  override protected def awaitContainersReady(): Unit = SharedTestContainers.awaitMysqlUp

  override protected def createOriginDatabase(): Unit = {
    MysqlEndToEndTestUtil.createDb(containers.origin.name, containers.origin.db.name)
  }

  override protected def createTargetDatabases(): Unit = {
    MysqlEndToEndTestUtil.createDb(containers.targetSingleThreaded.name, containers.targetSingleThreaded.db.name)
    MysqlEndToEndTestUtil.createDb(containers.targetAkkaStreams.name, containers.targetAkkaStreams.db.name)
  }

  override protected def containers: DatabaseContainerSet[MySqlDatabase] = {
    import SharedTestContainers._

    new DatabaseContainerSet[MySqlDatabase](
      MysqlEndToEndTestUtil.buildContainer(mysqlOrigin.name, testName, mysqlOrigin.db.port),
      MysqlEndToEndTestUtil.buildContainer(mysqlTargetSingleThreaded.name, testName, mysqlTargetSingleThreaded.db.port),
      MysqlEndToEndTestUtil.buildContainer(mysqlTargetAkkaStreams.name, testName, mysqlTargetAkkaStreams.db.port)
    )
  }

  override protected def prepareOriginDDL(): Unit

  override protected def prepareOriginDML(): Unit

  override protected def prepareTargetDDL(): Unit = {
    s"./src/test/trw.dbsubsetter.util/sync_mysql_origin_to_target.sh ${containers.origin.name} ${containers.origin.db.name} ${containers.targetSingleThreaded.name} ${containers.targetSingleThreaded.db.name}".!!
    s"./src/test/trw.dbsubsetter.util/sync_mysql_origin_to_target.sh ${containers.origin.name} ${containers.origin.db.name} ${containers.targetAkkaStreams.name} ${containers.targetAkkaStreams.db.name}".!!
  }

  override protected def postSubset(): Unit = {} // No-op
}

object MysqlEndToEndTestUtil {
  def buildContainer(containerName: String, dbName: String, dbPort: Int): MySqlContainer = {
    val db: MySqlDatabase = new MySqlDatabase(dbName, dbPort)
    new MySqlContainer(containerName, db)
  }

  def createDb(container: String, db: String): Unit = {
    s"./src/test/trw.dbsubsetter.util/create_mysql_db.sh $container $db".!!
  }
}