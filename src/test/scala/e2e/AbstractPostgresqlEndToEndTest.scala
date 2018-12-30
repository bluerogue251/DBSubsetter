package e2e

import util.db._

import scala.sys.process._

abstract class AbstractPostgresqlEndToEndTest extends AbstractEndToEndTest[PostgreSQLDatabase] {
  override protected val profile = slick.jdbc.PostgresProfile

  protected def testName: String

  protected def originPort: Int

  override protected def startOriginContainer():Unit = SharedTestContainers.postgres

  override protected def startTargetContainers(): Unit = {} // No-op (shares container with origin)

  override protected def awaitContainersReady(): Unit = Thread.sleep(4000)

  override protected def createOriginDatabase(): Unit = {
    createDb(containers.origin.name, containers.origin.db.name)
  }

  override protected def createTargetDatabases(): Unit = {
    createDb(containers.targetSingleThreaded.name, containers.targetSingleThreaded.db.name)
    createDb(containers.targetAkkaStreams.name, containers.targetAkkaStreams.db.name)
  }

  override protected def containers: DatabaseContainerSet[PostgreSQLDatabase] = {
    val containerName = SharedTestContainers.postgres.name
    val port = SharedTestContainers.postgres.db.port

    val originDb = s"${testName}_origin"
    val targetSingleThreadedDb = s"${testName}_target_single_threaded"
    val targetAkkaStreamsDb = s"${testName}_target_akka_streams"

    new DatabaseContainerSet(
      buildContainer(containerName, port, originDb),
      buildContainer(containerName, port, targetSingleThreadedDb),
      buildContainer(containerName, port, targetAkkaStreamsDb)
    )
  }

  override protected def prepareOriginDDL(): Unit

  override protected def prepareOriginDML(): Unit

  override protected def prepareTargetDDL(): Unit = {
    s"./src/test/util/sync_postgres_origin_to_target.sh ${containers.origin.name} ${containers.origin.db.name} ${containers.targetSingleThreaded.name} ${containers.targetSingleThreaded.db.name}".!!
    s"./src/test/util/sync_postgres_origin_to_target.sh ${containers.origin.name} ${containers.origin.db.name} ${containers.targetAkkaStreams.name} ${containers.targetAkkaStreams.db.name}".!!
  }

  override protected def postSubset(): Unit = {
    s"./src/test/util/postgres_post_subset.sh ${containers.origin.name} ${containers.origin.db.name} ${containers.targetSingleThreaded.name} ${containers.targetSingleThreaded.db.name}".!!
    s"./src/test/util/postgres_post_subset.sh ${containers.origin.name} ${containers.origin.db.name} ${containers.targetAkkaStreams.name} ${containers.targetAkkaStreams.db.name}".!!
  }

  private def buildContainer(containerName: String, dbPort: Int, dbName: String): PostgreSQLContainer = {
    val db: PostgreSQLDatabase = new PostgreSQLDatabase(dbName, dbPort)
    new PostgreSQLContainer(containerName, db)
  }

  private def createDb(containerName: String, dbName: String): Unit = {
    s"docker exec $containerName createdb --user postgres $dbName".!!
  }
}
