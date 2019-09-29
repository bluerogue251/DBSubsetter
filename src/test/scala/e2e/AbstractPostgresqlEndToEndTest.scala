package e2e

import util.db._

import scala.sys.process._

abstract class AbstractPostgresqlEndToEndTest extends AbstractEndToEndTest[PostgreSQLDatabase] {
  override protected val profile = slick.jdbc.PostgresProfile

  protected def testName: String

  override protected def startOriginContainer():Unit = SharedTestContainers.postgres

  override protected def startTargetContainers(): Unit = {} // No-op (shares container with origin)

  override protected def awaitContainersReady(): Unit = SharedTestContainers.awaitPostgresUp

  override protected def createOriginDatabase(): Unit = {
    PostgresqlEndToEndTestUtil.createDb(containers.origin.db)
  }

  override protected def createTargetDatabases(): Unit = {
    PostgresqlEndToEndTestUtil.createDb(containers.targetSingleThreaded.db)
    PostgresqlEndToEndTestUtil.createDb(containers.targetAkkaStreams.db)
  }

  override protected def containers: DatabaseContainerSet[PostgreSQLDatabase] = {
    val host = SharedTestContainers.postgres.db.host
    val port = SharedTestContainers.postgres.db.port

    val originDb = s"${testName}_origin"
    val targetSingleThreadedDb = s"${testName}_target_single_threaded"
    val targetAkkaStreamsDb = s"${testName}_target_akka_streams"

    new DatabaseContainerSet(
      PostgresqlEndToEndTestUtil.buildContainer(host, port, originDb),
      PostgresqlEndToEndTestUtil.buildContainer(host, port, targetSingleThreadedDb),
      PostgresqlEndToEndTestUtil.buildContainer(host, port, targetAkkaStreamsDb)
    )
  }

  override protected def prepareOriginDDL(): Unit

  override protected def prepareOriginDML(): Unit

  override protected def prepareTargetDDL(): Unit = {
    PostgresqlEndToEndTestUtil.preSubsetDdlSync(containers.origin.db, containers.targetSingleThreaded.db)
    PostgresqlEndToEndTestUtil.preSubsetDdlSync(containers.origin.db, containers.targetAkkaStreams.db)
  }

  override protected def postSubset(): Unit = {
    PostgresqlEndToEndTestUtil.postSubsetDdlSync(containers.origin.db, containers.targetSingleThreaded.db)
    PostgresqlEndToEndTestUtil.postSubsetDdlSync(containers.origin.db, containers.targetAkkaStreams.db)
  }
}

object PostgresqlEndToEndTestUtil {
  def buildContainer(dbHost: String, dbPort: Int, dbName: String): PostgreSQLContainer = {
    val db: PostgreSQLDatabase = new PostgreSQLDatabase(dbHost, dbPort, dbName)
    new PostgreSQLContainer("placeholder-do-not-user", db)
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
