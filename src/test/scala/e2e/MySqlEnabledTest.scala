package e2e

import util.Ports
import util.db._

import scala.sys.process._
import scala.util.Properties

/**
  * A test which requires access to a MySql database.
  */
abstract class MySqlEnabledTest extends DbEnabledTest[MySqlDatabase] {
  override protected val profile = slick.jdbc.MySQLProfile

  protected def additionalSchemas: List[String] = List.empty

  protected def testName: String

  override protected def createOriginDatabase(): Unit = {
    MysqlEndToEndTestUtil.createSchemas(dbs.origin, dbs.origin.name :: additionalSchemas)
  }

  override protected def createTargetDatabases(): Unit = {
    MysqlEndToEndTestUtil.createSchemas(dbs.targetSingleThreaded, dbs.targetSingleThreaded.name :: additionalSchemas)
    MysqlEndToEndTestUtil.createSchemas(dbs.targetAkkaStreams, dbs.targetAkkaStreams.name :: additionalSchemas)
  }

  override protected def dbs: DatabaseSet[MySqlDatabase] = {
    val mySqlOriginHost: String =
      Properties.envOrElse("DB_SUBSETTER_MYSQL_ORIGIN_HOST", "0.0.0.0")

    val mySqlTargetSingleThreadedHost: String =
      Properties.envOrElse("DB_SUBSETTER_MYSQL_TARGET_SINGLE_THREADED_HOST", "0.0.0.0")

    val mySqlTargetAkkaStreamsHost: String =
      Properties.envOrElse("DB_SUBSETTER_MYSQL_TARGET_AKKA_STREAMS_HOST", "0.0.0.0")

    val mySqlOriginPort: Int =
      Properties.envOrElse("DB_SUBSETTER_MYSQL_ORIGIN_PORT", Ports.sharedMySqlOriginPort.toString).toInt

    val mySqlTargetSingleThreadedPort: Int =
      Properties.envOrElse("DB_SUBSETTER_MYSQL_TARGET_SINGLE_THREADED_PORT", Ports.sharedMySqlTargetSingleThreadedPort.toString).toInt

    val mySqlTargetAkkaStreamsPort: Int =
      Properties.envOrElse("DB_SUBSETTER_MYSQL_TARGET_AKKA_STREAMS_PORT", Ports.sharedMySqlTargetAkkaStreamsPort.toString).toInt

    lazy val mysqlOrigin: MySqlDatabase = buildDatabase(mySqlOriginHost, mySqlOriginPort)

    lazy val mysqlTargetSingleThreaded: MySqlDatabase = buildDatabase(mySqlTargetSingleThreadedHost, mySqlTargetSingleThreadedPort)

    lazy val mysqlTargetAkkaStreams: MySqlDatabase = buildDatabase(mySqlTargetAkkaStreamsHost, mySqlTargetAkkaStreamsPort)

    def buildDatabase(host: String, port: Int): MySqlDatabase = {
      new MySqlDatabase(host, port, testName)
    }

    new DatabaseSet[MySqlDatabase](
      mysqlOrigin,
      mysqlTargetSingleThreaded,
      mysqlTargetAkkaStreams
    )
  }

  override protected def prepareOriginDDL(): Unit

  override protected def prepareOriginDML(): Unit

  override protected def prepareTargetDDL(): Unit = {
    val allSchemas: List[String] = dbs.origin.name :: additionalSchemas
    MysqlEndToEndTestUtil.preSubsetDdlSync(dbs.origin, dbs.targetSingleThreaded, allSchemas)
    MysqlEndToEndTestUtil.preSubsetDdlSync(dbs.origin, dbs.targetAkkaStreams, allSchemas)
  }
}

object MysqlEndToEndTestUtil {
  def createSchemas(db: MySqlDatabase, schemas: List[String]): Unit = {
    schemas.foreach(schema => {
      s"./src/test/util/create_mysql_db.sh ${db.host} ${db.port} $schema".!!
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