package e2e

import util.Ports
import util.db._

import scala.sys.process._
import scala.util.Properties

abstract class AbstractMysqlEndToEndTest extends AbstractEndToEndTest[MySqlDatabase] {
  override protected val profile = slick.jdbc.MySQLProfile

  protected def additionalSchemas: List[String] = List.empty

  protected def testName: String

  override protected def startOriginContainer():Unit = {} // No-Op

  override protected def startTargetContainers(): Unit = {} // No-Op

  override protected def awaitContainersReady(): Unit = {} // No-Op

  override protected def createOriginDatabase(): Unit = {
    MysqlEndToEndTestUtil.createSchemas(containers.origin.db, containers.origin.db.name :: additionalSchemas)
  }

  override protected def createTargetDatabases(): Unit = {
    MysqlEndToEndTestUtil.createSchemas(containers.targetSingleThreaded.db, containers.targetSingleThreaded.db.name :: additionalSchemas)
    MysqlEndToEndTestUtil.createSchemas(containers.targetAkkaStreams.db, containers.targetAkkaStreams.db.name :: additionalSchemas)
  }

  override protected def containers: DatabaseContainerSet[MySqlDatabase] = {
    val mySqlOriginPort: Int =
      Properties.envOrElse("DB_SUBSETTER_MYSQL_ORIGIN_PORT", Ports.sharedMySqlOriginPort.toString).toInt

    val mySqlTargetSingleThreadedPort: Int =
      Properties.envOrElse("DB_SUBSETTER_MYSQL_TARGET_SINGLE_THREADED_PORT", Ports.sharedMySqlTargetSingleThreadedPort.toString).toInt

    val mySqlTargetAkkaStreamsPort: Int =
      Properties.envOrElse("DB_SUBSETTER_MYSQL_TARGET_AKKA_STREAMS_PORT", Ports.sharedMySqlTargetAkkaStreamsPort.toString).toInt

    lazy val mysqlOrigin: DatabaseContainer[MySqlDatabase] = buildMysqlContainer(mySqlOriginPort)
    lazy val mysqlTargetSingleThreaded: DatabaseContainer[MySqlDatabase] = buildMysqlContainer(mySqlTargetSingleThreadedPort)
    lazy val mysqlTargetAkkaStreams: DatabaseContainer[MySqlDatabase] = buildMysqlContainer(mySqlTargetAkkaStreamsPort)

    def buildMysqlContainer(port: Int): MySqlContainer = {
      val dbHost: String = Properties.envOrElse("DB_SUBSETTER_MYSQL_HOST", "0.0.0.0")
      val db = new MySqlDatabase(dbHost, port, testName)
      new MySqlContainer("placholder-do-not-use", db)
    }

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
      s"""mysql --host ${db.host} --port ${db.port} --user root -e "drop database if exists $schema"""".!!
      s"""mysql --host ${db.host} --port ${db.port} --user root -e "create database $schema"""".!!
    })
  }

  def preSubsetDdlSync(origin: MySqlDatabase, target: MySqlDatabase, schemas: List[String]): Unit = {
    schemas.foreach(schema => {
      val exportCommand: String = s"mysqldump --host ${origin.host} --port ${origin.port} --user root --no-data $schema"
      val importCommand: String = s"mysql --host ${target.host} --port ${target.port} --user root $schema"
      (exportCommand #| importCommand).!!
    })
  }
}