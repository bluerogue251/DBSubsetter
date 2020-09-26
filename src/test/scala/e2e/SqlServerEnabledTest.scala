package e2e

import util.Ports
import util.db._

import scala.sys.process._
import scala.util.Properties

abstract class SqlServerEnabledTest extends DbEnabledTest[SqlServerDatabase] {
  override protected val profile = slick.jdbc.SQLServerProfile

  protected def testName: String

  override protected def createOriginDatabase(): Unit = {
    createEmptyDb(dbs.origin.name, dbs.origin.name)
  }

  override protected def createTargetDatabases(): Unit = {
    createEmptyDb(dbs.origin.name, dbs.targetSingleThreaded.name)
    createEmptyDb(dbs.origin.name, dbs.targetAkkaStreams.name)
    Thread.sleep(2000) // Try to get around flaky SqlServer tests
  }

  override protected def dbs: DatabaseSet[SqlServerDatabase] = {
    val host = Properties.envOrElse("DB_SUBSETTER_SQL_SERVER_HOST", "localhost")
    val port = Ports.sharedSqlServerPort
    val originDbName = s"${testName}_origin"
    val targetSingleThreadedDbName = s"${testName}_target_single_threaded"
    val targetAkkaStreamsDbName = s"${testName}_target_akka_streams"

    new DatabaseSet(
      buildDatabase(host, originDbName, port),
      buildDatabase(host, targetSingleThreadedDbName, port),
      buildDatabase(host, targetAkkaStreamsDbName, port)
    )
  }

  override protected def prepareOriginDDL(): Unit

  override protected def prepareOriginDML(): Unit

  override protected def prepareTargetDDL(): Unit = {
    s"./src/test/util/sync_sqlserver_origin_to_target.sh ${dbs.origin.host} ${dbs.origin.name} ${dbs.targetSingleThreaded.name}".!!
    s"./src/test/util/sync_sqlserver_origin_to_target.sh ${dbs.origin.host} ${dbs.origin.name} ${dbs.targetAkkaStreams.name}".!!
  }

  override protected def postSubset(): Unit = {
    s"./src/test/util/sqlserver_post_subset.sh ${dbs.origin.host} ${dbs.targetSingleThreaded.name}".!!
    s"./src/test/util/sqlserver_post_subset.sh ${dbs.origin.host} ${dbs.targetAkkaStreams.name}".!!
  }

  private def createEmptyDb(containerName: String, dbName: String): Unit = {
    s"./src/test/util/create_sqlserver_db.sh ${dbs.origin.host} $dbName".!!
  }

  private def buildDatabase(dbHost: String, dbName: String, dbPort: Int): SqlServerDatabase = {
    new SqlServerDatabase(dbHost, dbName, dbPort)
  }
}
