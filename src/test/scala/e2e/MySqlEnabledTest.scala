package e2e

import util.Ports
import util.db._

import scala.sys.process._
import scala.util.Properties

/**
  * A test which requires access to a running MySql database.
  */
abstract class MySqlEnabledTest extends DbEnabledTest[MySqlDatabase] {
  override protected val profile = slick.jdbc.MySQLProfile

  protected def testName: String

  override protected def createOriginDatabase(): Unit = {
    MysqlEndToEndTestUtil.createSchemas(dbs.origin, additionalSchemas + dbs.origin.name)
  }

  override protected def createTargetDatabase(): Unit = {
    MysqlEndToEndTestUtil.createSchemas(dbs.target, additionalSchemas + dbs.target.name)
  }

  override protected def dbs: DatabaseSet[MySqlDatabase] = {
    val mySqlOriginHost: String =
      Properties.envOrElse("DB_SUBSETTER_MYSQL_ORIGIN_HOST", "0.0.0.0")

    val mySqlTargetHost: String =
      Properties.envOrElse("DB_SUBSETTER_MYSQL_TARGET_HOST", "0.0.0.0")

    val mySqlOriginPort: Int =
      Properties.envOrElse("DB_SUBSETTER_MYSQL_ORIGIN_PORT", Ports.sharedMySqlOriginPort.toString).toInt

    val mySqlTargetPort: Int =
      Properties
        .envOrElse("DB_SUBSETTER_MYSQL_TARGET_PORT", Ports.sharedMySqlTargetPort.toString)
        .toInt

    lazy val mysqlOrigin: MySqlDatabase =
      buildDatabase(mySqlOriginHost, mySqlOriginPort)

    lazy val mysqlTarget: MySqlDatabase =
      buildDatabase(mySqlTargetHost, mySqlTargetPort)

    def buildDatabase(host: String, port: Int): MySqlDatabase = {
      new MySqlDatabase(host, port, testName)
    }

    new DatabaseSet[MySqlDatabase](mysqlOrigin, mysqlTarget)
  }

  override protected def prepareOriginSchemas(): Unit = {}

  override protected def prepareOriginDDL(): Unit

  override protected def prepareOriginDML(): Unit

  override protected def prepareTargetDDL(): Unit = {
    val allSchemas: Set[String] = additionalSchemas + dbs.origin.name
    MysqlEndToEndTestUtil.preSubsetDdlSync(dbs.origin, dbs.target, allSchemas)
  }
}

object MysqlEndToEndTestUtil {
  def createSchemas(db: MySqlDatabase, schemas: Set[String]): Unit = {
    schemas.foreach(schema => {
      s"./src/test/util/create_mysql_db.sh ${db.host} ${db.port} $schema".!!
    })
  }

  def preSubsetDdlSync(origin: MySqlDatabase, target: MySqlDatabase, schemas: Set[String]): Unit = {
    schemas.foreach(schema => {
      val exportCommand: String =
        s"mysqldump --host ${origin.host} --port ${origin.port} --user root --no-data $schema"
      val importCommand: String =
        s"mysql --host ${target.host} --port ${target.port} --user root $schema"
      (exportCommand #| importCommand).!!
    })
  }
}
