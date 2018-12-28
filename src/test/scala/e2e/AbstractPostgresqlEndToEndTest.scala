package e2e

import util.db._

import scala.sys.process._

abstract class AbstractPostgresqlEndToEndTest extends AbstractEndToEndTest[PostgreSQLDatabase] {
  override protected val profile = slick.jdbc.PostgresProfile

  protected def testName: String

  protected def originPort: Int

  override protected def startContainers(): DatabaseContainerSet[PostgreSQLDatabase] = {
    val originContainerName = s"${testName}_origin_postgres"
    val targetSingleThreadedContainerName = s"${testName}_target_sith_postgres"
    val targetAkkaStreamsContainerName = s"${testName}_target_akst_postgres"

    val targetSingleThreadedPort: Int = originPort + 1
    val targetAkkaStreamsPort: Int = originPort + 2

    DatabaseContainer.startPostgreSQL(originContainerName, originPort)
    DatabaseContainer.startPostgreSQL(targetSingleThreadedContainerName, targetSingleThreadedPort)
    DatabaseContainer.startPostgreSQL(targetAkkaStreamsContainerName, targetAkkaStreamsPort)

    Thread.sleep(5000)

    val originContainer = buildContainer(originContainerName, testName, originPort)
    val targetSingleThreadedContainer = buildContainer(targetSingleThreadedContainerName, testName, targetSingleThreadedPort)
    val targetAkkaStreamsContainer = buildContainer(targetAkkaStreamsContainerName, testName, targetAkkaStreamsPort)

    new DatabaseContainerSet(originContainer, targetSingleThreadedContainer, targetAkkaStreamsContainer)
  }

  override protected def createEmptyDatabases(): Unit = {
    createDb(containers.origin.name)
    createDb(containers.targetSingleThreaded.name)
    createDb(containers.targetAkkaStreams.name)
  }

  override protected def prepareOriginDDL(): Unit

  override protected def prepareOriginDML(): Unit

  override protected def prepareTargetDDL(): Unit = {
    s"./src/test/util/sync_postgres_origin_to_target.sh $testName ${containers.origin.name} ${containers.targetSingleThreaded.name}".!!
    s"./src/test/util/sync_postgres_origin_to_target.sh $testName ${containers.origin.name} ${containers.targetAkkaStreams.name}".!!
  }

  override protected def postSubset(): Unit = {
    s"./src/test/util/postgres_post_subset.sh $testName ${containers.origin.name} ${containers.targetSingleThreaded.name}".!!
    s"./src/test/util/postgres_post_subset.sh $testName ${containers.origin.name} ${containers.targetAkkaStreams.name}".!!
  }

  private def buildContainer(containerName: String, dbName: String, dbPort: Int): PostgreSQLContainer = {
    val db: PostgreSQLDatabase = new PostgreSQLDatabase(dbName, dbPort)
    new PostgreSQLContainer(containerName, db)
  }

  private def createDb(dockerContainer: String): Unit = {
    s"docker exec $dockerContainer createdb --user postgres $testName".!!
  }
}
