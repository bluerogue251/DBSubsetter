package e2e

import util.Ports
import util.db._

import scala.sys.process._
import scala.util.Properties

/**
  * A test which requires access to a running Microsoft Sql Server database.
  */
abstract class SqlServerEnabledTest extends DbEnabledTest[SqlServerDatabase] {
  override protected val profile = slick.jdbc.SQLServerProfile

  protected def testName: String

  override protected def createOriginDatabase(): Unit = {
    createEmptyDb(dbs.origin.name)
  }

  override protected def createTargetDatabase(): Unit = {
    createEmptyDb(dbs.target.name)
    Thread.sleep(2000) // Try to get around flaky SqlServer tests
  }

  override protected def dbs: DatabaseSet[SqlServerDatabase] = {
    val host = Properties.envOrElse("DB_SUBSETTER_SQL_SERVER_HOST", "localhost")
    val port = Ports.sharedSqlServerPort
    val originDbName = s"${testName}_origin"
    val targetDbName = s"${testName}_target"

    new DatabaseSet(
      buildDatabase(host, originDbName, port),
      buildDatabase(host, targetDbName, port)
    )
  }

  override protected def prepareOriginSchemas(): Unit = {
    additionalSchemas.foreach { schema =>
      s"./src/test/util/create_schema_sqlserver.sh ${dbs.origin.host} ${dbs.origin.name} $schema".!!
    }
  }

  override protected def prepareOriginDDL(): Unit

  override protected def prepareOriginDML(): Unit

  override protected def prepareTargetDDL(): Unit = {
    s"./src/test/util/sync_sqlserver_origin_to_target.sh ${dbs.origin.host} ${dbs.origin.name} ${dbs.target.name}".!!
  }

  private def createEmptyDb(dbName: String): Unit = {
    s"./src/test/util/create_sqlserver_db.sh ${dbs.origin.host} $dbName".!!
  }

  private def buildDatabase(dbHost: String, dbName: String, dbPort: Int): SqlServerDatabase = {
    new SqlServerDatabase(dbHost, dbName, dbPort)
  }
}
