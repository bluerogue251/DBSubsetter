package e2e

import util.db._
import util.retry.RetryUtil

import scala.sys.process._

abstract class AbstractPostgresqlEndToEndTest extends AbstractEndToEndTest[PostgreSQLDatabase] {
  override protected val profile = slick.jdbc.PostgresProfile

  protected def testName: String

  protected def originPort: Int

  private def originContainerName = s"${testName}_origin_postgres"

  private def targetSingleThreadedContainerName = s"${testName}_target_sith_postgres"

  private def targetAkkaStreamsContainerName = s"${testName}_target_akst_postgres"

  private def targetSingleThreadedPort: Int = originPort + 1

  private def targetAkkaStreamsPort: Int = originPort + 2

  override protected def startOriginContainer():Unit = {
    DatabaseContainer.startPostgreSQL(originContainerName, originPort)
  }

  override protected def startTargetContainers(): Unit = {
    DatabaseContainer.startPostgreSQL(targetSingleThreadedContainerName, targetSingleThreadedPort)
    DatabaseContainer.startPostgreSQL(targetAkkaStreamsContainerName, targetAkkaStreamsPort)
  }

  override protected def createOriginDatabase(): Unit = {
    createDb(containers.origin.name, containers.origin.db.name)
  }

  override protected def createTargetDatabases(): Unit = {
    createDb(containers.targetSingleThreaded.name, containers.targetSingleThreaded.db.name)
    createDb(containers.targetAkkaStreams.name, containers.targetAkkaStreams.db.name)
  }

  override protected def containers: DatabaseContainerSet[PostgreSQLDatabase] = {
    new DatabaseContainerSet(
      buildContainer(originContainerName, testName, originPort),
      buildContainer(targetSingleThreadedContainerName, testName, targetSingleThreadedPort),
      buildContainer(targetAkkaStreamsContainerName, testName, targetAkkaStreamsPort)
    )
  }

  override protected def prepareOriginDDL(): Unit

  override protected def prepareOriginDML(): Unit

  override protected def prepareTargetDDL(): Unit = {
    def sync(target: String): Unit = {
      s"./src/test/util/sync_postgres_origin_to_target.sh ${containers.origin.db.name} ${containers.origin.name} $target".!!
    }
    sync(containers.targetSingleThreaded.name)
    sync(containers.targetAkkaStreams.name)
  }

  override protected def postSubset(): Unit = {
    s"./src/test/util/postgres_post_subset.sh ${containers.origin.db.name} ${containers.origin.name} ${containers.targetSingleThreaded.name}".!!
    s"./src/test/util/postgres_post_subset.sh ${containers.origin.db.name} ${containers.origin.name} ${containers.targetAkkaStreams.name}".!!
  }

  private def buildContainer(containerName: String, dbName: String, dbPort: Int): PostgreSQLContainer = {
    val db: PostgreSQLDatabase = new PostgreSQLDatabase(dbName, dbPort)
    new PostgreSQLContainer(containerName, db)
  }

  private def createDb(containerName: String, dbName: String): Unit = {
    val command = s"docker exec $containerName createdb --user postgres $dbName"
    RetryUtil.withRetry(command)
  }
}
