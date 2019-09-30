package e2e

import util.db._

import scala.sys.process._

abstract class AbstractMysqlEndToEndTest extends AbstractEndToEndTest[MySqlDatabase] {
  override protected val profile = slick.jdbc.MySQLProfile

  protected def testName: String

  override protected def startOriginContainer():Unit = SharedTestContainers.mysqlOrigin

  override protected def startTargetContainers(): Unit = {
    SharedTestContainers.mysqlTargetSingleThreaded
    SharedTestContainers.mysqlTargetAkkaStreams
  }

  override protected def awaitContainersReady(): Unit = {} // No-Op

  override protected def createOriginDatabase(): Unit = {
    MysqlEndToEndTestUtil.createDb(containers.origin.db)
  }

  override protected def createTargetDatabases(): Unit = {
    MysqlEndToEndTestUtil.createDb(containers.targetSingleThreaded.db)
    MysqlEndToEndTestUtil.createDb(containers.targetAkkaStreams.db)
  }

  override protected def containers: DatabaseContainerSet[MySqlDatabase] = {
    import SharedTestContainers._

    new DatabaseContainerSet[MySqlDatabase](
      mysqlOrigin,
      mysqlTargetSingleThreaded,
      mysqlTargetAkkaStreams
    )
  }

  override protected def prepareOriginDDL(): Unit

  override protected def prepareOriginDML(): Unit

  override protected def prepareTargetDDL(): Unit = {
    s"./src/test/util/sync_mysql_origin_to_target.sh ${containers.origin.name} ${containers.origin.db.name} ${containers.targetSingleThreaded.name} ${containers.targetSingleThreaded.db.name}".!!
    s"./src/test/util/sync_mysql_origin_to_target.sh ${containers.origin.name} ${containers.origin.db.name} ${containers.targetAkkaStreams.name} ${containers.targetAkkaStreams.db.name}".!!
  }

  override protected def postSubset(): Unit = {} // No-op
}

object MysqlEndToEndTestUtil {
  def createDb(db: MySqlDatabase): Unit = {
    s"""mysql --host ${db.host} --port ${db.port} --user root -e "create database ${db.name}""".!!
  }

  def preSubsetDdlSync(origin: MySqlDatabase, target: MySqlDatabase): Unit = {
    val exportCommand: String = s"mysqldump --host${origin.host} --port ${origin.port} --user root --no-data ${origin.name}"
    val importCommand: String = s"mysql -host ${target.host} --port ${target.port} --user root ${target.name}"
    (exportCommand #| importCommand).!!
  }
}