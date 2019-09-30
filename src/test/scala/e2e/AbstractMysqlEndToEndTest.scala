package e2e

import util.db._

import scala.sys.process._

abstract class AbstractMysqlEndToEndTest extends AbstractEndToEndTest[MySqlDatabase] {
  override protected val profile = slick.jdbc.MySQLProfile

  protected def additionalSchemas: List[String] = List.empty

  protected def testName: String

  override protected def startOriginContainer():Unit = SharedTestContainers.mysqlOrigin

  override protected def startTargetContainers(): Unit = {
    SharedTestContainers.mysqlTargetSingleThreaded
    SharedTestContainers.mysqlTargetAkkaStreams
  }

  override protected def awaitContainersReady(): Unit = {} // No-Op

  override protected def createOriginDatabase(): Unit = {
    MysqlEndToEndTestUtil.createSchemas(containers.origin.db, containers.origin.db.name :: additionalSchemas)
  }

  override protected def createTargetDatabases(): Unit = {
    MysqlEndToEndTestUtil.createSchemas(containers.targetSingleThreaded.db, containers.targetSingleThreaded.db.name :: additionalSchemas)
    MysqlEndToEndTestUtil.createSchemas(containers.targetAkkaStreams.db, containers.targetAkkaStreams.db.name :: additionalSchemas)
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
    val allSchemas: List[String] = containers.origin.db.name :: additionalSchemas
    MysqlEndToEndTestUtil.preSubsetDdlSync(containers.origin.db, containers.targetSingleThreaded.db, allSchemas)
    MysqlEndToEndTestUtil.preSubsetDdlSync(containers.origin.db, containers.targetAkkaStreams.db, allSchemas)
  }

  override protected def postSubset(): Unit = {} // No-op
}

object MysqlEndToEndTestUtil {
  def createSchemas(db: MySqlDatabase, schemas: List[String]): Unit = {
    schemas.foreach(schema => {
      s"""mysql --host ${db.host} --port ${db.port} --user root -e "create database $schema""".!!
    })
  }

  def preSubsetDdlSync(origin: MySqlDatabase, target: MySqlDatabase, schemas: List[String]): Unit = {
    schemas.foreach(schema => {
      val exportCommand: String = s"mysqldump --host${origin.host} --port ${origin.port} --user root --no-data $schema"
      val importCommand: String = s"mysql -host ${target.host} --port ${target.port} --user root $schema"
      (exportCommand #| importCommand).!!
    })
  }
}