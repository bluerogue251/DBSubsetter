package e2e

import util.Ports
import util.db._

import scala.sys.process._
import scala.util.Properties

abstract class PostgresEnabledTest extends DbEnabledTest[PostgreSQLDatabase] {
  override protected val profile = slick.jdbc.PostgresProfile

  protected def testName: String

  override protected def createOriginDatabase(): Unit = {
    PostgresqlEndToEndTestUtil.createDb(dbs.origin)
  }

  override protected def createTargetDatabases(): Unit = {
    PostgresqlEndToEndTestUtil.createDb(dbs.targetSingleThreaded)
    PostgresqlEndToEndTestUtil.createDb(dbs.targetAkkaStreams)
  }

  override protected def dbs: DatabaseSet[PostgreSQLDatabase] = {
    val host = Properties.envOrElse("DB_SUBSETTER_POSTGRES_HOST", "localhost")
    val port = Ports.sharedPostgresPort

    val originDb = s"${testName}_origin"
    val targetSingleThreadedDb = s"${testName}_target_single_threaded"
    val targetAkkaStreamsDb = s"${testName}_target_akka_streams"

    new DatabaseSet(
      PostgresqlEndToEndTestUtil.buildDatabase(host, port, originDb),
      PostgresqlEndToEndTestUtil.buildDatabase(host, port, targetSingleThreadedDb),
      PostgresqlEndToEndTestUtil.buildDatabase(host, port, targetAkkaStreamsDb)
    )
  }

  override protected def prepareOriginDDL(): Unit

  override protected def prepareOriginDML(): Unit

  override protected def prepareTargetDDL(): Unit = {
    PostgresqlEndToEndTestUtil.preSubsetDdlSync(dbs.origin, dbs.targetSingleThreaded)
    PostgresqlEndToEndTestUtil.preSubsetDdlSync(dbs.origin, dbs.targetAkkaStreams)
  }

  override protected def postSubset(): Unit = {
    PostgresqlEndToEndTestUtil.postSubsetDdlSync(dbs.origin, dbs.targetSingleThreaded)
    PostgresqlEndToEndTestUtil.postSubsetDdlSync(dbs.origin, dbs.targetAkkaStreams)
  }
}

object PostgresqlEndToEndTestUtil {
  def buildDatabase(dbHost: String, dbPort: Int, dbName: String): PostgreSQLDatabase = {
    new PostgreSQLDatabase(dbHost, dbPort, dbName)
  }

  def createDb(db: PostgreSQLDatabase): Unit = {
    s"dropdb --host ${db.host} --port ${db.port} --user postgres --if-exists ${db.name}".!!
    s"createdb --host ${db.host} --port ${db.port} --user postgres ${db.name}".!!
  }

  def preSubsetDdlSync(origin: PostgreSQLDatabase, target: PostgreSQLDatabase): Unit = {
    val exportCommand =
      s"pg_dump --host ${origin.host} --port ${origin.port} --user postgres --section=pre-data ${origin.name}"

    val importCommand =
      s"psql --host ${target.host} --port ${target.port} --user postgres ${target.name}"

    (exportCommand #| importCommand).!!
  }

  def postSubsetDdlSync(origin: PostgreSQLDatabase, target: PostgreSQLDatabase): Unit = {
    val exportCommand =
      s"pg_dump --host ${origin.host} --port ${origin.port} --user postgres --section=post-data ${origin.name}"

    val importCommand =
      s"psql --host ${target.host} --port ${target.port} --user postgres ${target.name} -v ON_ERROR_STOP=1"

    (exportCommand #| importCommand).!!
  }
}
