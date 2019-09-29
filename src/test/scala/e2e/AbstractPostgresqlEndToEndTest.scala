package e2e

import util.db._

import scala.sys.process._

abstract class AbstractPostgresqlEndToEndTest extends AbstractEndToEndTest[PostgreSQLDatabase] {
  override protected val profile = slick.jdbc.PostgresProfile

  val adminDb: PostgreSQLDatabase = new PostgreSQLDatabase("postgres", "localhost", SharedTestContainers.postgres.db.port)

  protected def testName: String

  override protected def startOriginContainer():Unit = {} // No-op

  override protected def startTargetContainers(): Unit = {} // No-op (shares container with origin)

  override protected def awaitContainersReady(): Unit = {} // No-op

  override protected def createOriginDatabase(): Unit = {
    PostgresqlEndToEndTestUtil.createDb(adminDb, containers.origin.db.name)
  }

  override protected def createTargetDatabases(): Unit = {
    PostgresqlEndToEndTestUtil.createDb(adminDb, containers.targetSingleThreaded.db.name)
    PostgresqlEndToEndTestUtil.createDb(adminDb, containers.targetAkkaStreams.db.name)
  }

  override protected def containers: DatabaseContainerSet[PostgreSQLDatabase] = {
    val containerName = "placeholder"
    val port = SharedTestContainers.postgres.db.port

    val originDb = s"${testName}_origin"
    val targetSingleThreadedDb = s"${testName}_target_single_threaded"
    val targetAkkaStreamsDb = s"${testName}_target_akka_streams"

    new DatabaseContainerSet(
      PostgresqlEndToEndTestUtil.buildContainer(containerName, port, originDb),
      PostgresqlEndToEndTestUtil.buildContainer(containerName, port, targetSingleThreadedDb),
      PostgresqlEndToEndTestUtil.buildContainer(containerName, port, targetAkkaStreamsDb)
    )
  }

  override protected def prepareOriginDDL(): Unit

  override protected def prepareOriginDML(): Unit

  override protected def prepareTargetDDL(): Unit = {
    PostgresqlEndToEndTestUtil.syncDDL(containers.origin.name, containers.origin.db.name, containers.targetSingleThreaded.name, containers.targetSingleThreaded.db.name)
    PostgresqlEndToEndTestUtil.syncDDL(containers.origin.name, containers.origin.db.name, containers.targetAkkaStreams.name, containers.targetAkkaStreams.db.name)
  }

  override protected def postSubset(): Unit = {
    s"./src/test/util/postgres_post_subset.sh ${containers.origin.name} ${containers.origin.db.name} ${containers.targetSingleThreaded.name} ${containers.targetSingleThreaded.db.name}".!!
    s"./src/test/util/postgres_post_subset.sh ${containers.origin.name} ${containers.origin.db.name} ${containers.targetAkkaStreams.name} ${containers.targetAkkaStreams.db.name}".!!
  }
}

object PostgresqlEndToEndTestUtil {
  def buildContainer(containerName: String, dbPort: Int, dbName: String): PostgreSQLContainer = {
    val db: PostgreSQLDatabase = new PostgreSQLDatabase(dbName, "localhost", dbPort)
    new PostgreSQLContainer(containerName, db)
  }

  def createDb(db: PostgreSQLDatabase, newDatabaseName: String): Unit = {
    SqlExecutor.execute(db, s"drop database if exists $newDatabaseName")
    SqlExecutor.execute(db, s"create database $newDatabaseName")
  }

  def syncDDL(originContainer: String, originDb: String, targetContainer: String, targetDb: String): Unit = {
    s"./src/test/util/sync_postgres_origin_to_target.sh $originContainer $originDb $targetContainer $targetDb".!!
  }
}
