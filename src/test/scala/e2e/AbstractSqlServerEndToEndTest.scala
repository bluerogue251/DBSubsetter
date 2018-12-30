package e2e

import util.db._
import util.retry.RetryUtil

import scala.sys.process._

/*
 * Purposely shares a single container between origin and target DBs
*/
abstract class AbstractSqlServerEndToEndTest extends AbstractEndToEndTest[SqlServerDatabase] {
  override protected val profile = slick.jdbc.SQLServerProfile

  protected def testName: String

  protected def port: Int

  override protected def startOriginContainer():Unit = {
    DatabaseContainer.startSqlServer(containers.origin.name, port)
  }

  override protected def startTargetContainers(): Unit = {} // No-op (container is shared with origin)

  override protected def createOriginDatabase(): Unit = {
    createEmptyDb(containers.origin.name, containers.origin.db.name)
  }

  override protected def createTargetDatabases(): Unit = {
    createEmptyDb(containers.origin.name, containers.targetSingleThreaded.db.name)
    createEmptyDb(containers.origin.name, containers.targetAkkaStreams.db.name)
  }

  override protected def containers: DatabaseContainerSet[SqlServerDatabase] = {
    val containerName = s"${testName}_sqlserver"
    val originDbName = s"${testName}_origin"
    val targetSingleThreadedDbName = s"${testName}_target_single_threaded"
    val targetAkkaStreamsDbName = s"${testName}_target_akka_streams"

    new DatabaseContainerSet(
      buildContainer(containerName, originDbName, port),
      buildContainer(containerName, targetSingleThreadedDbName, port),
      buildContainer(containerName, targetAkkaStreamsDbName, port)
    )
  }

  override protected def prepareOriginDDL(): Unit

  override protected def prepareOriginDML(): Unit

  override protected def prepareTargetDDL(): Unit = {
    s"./src/test/util/sync_sqlserver_origin_to_target.sh ${containers.origin.name} ${containers.origin.db.name} ${containers.targetSingleThreaded.db.name}".!!
    s"./src/test/util/sync_sqlserver_origin_to_target.sh ${containers.origin.name} ${containers.origin.db.name} ${containers.targetAkkaStreams.db.name}".!!
  }

  override protected def postSubset(): Unit = {
    s"./src/test/util/sqlserver_post_subset.sh ${containers.origin.name} ${containers.targetSingleThreaded.db.name}".!!
    s"./src/test/util/sqlserver_post_subset.sh ${containers.origin.name} ${containers.targetAkkaStreams.db.name}".!!
  }

  private def createEmptyDb(containerName: String, dbName: String): Unit = {
    RetryUtil.withRetry(s"./src/test/util/create_sqlserver_db.sh $containerName $dbName")
  }

  private def buildContainer(containerName: String, dbName: String, dbPort: Int): SqlServerContainer = {
    val db: SqlServerDatabase = new SqlServerDatabase(dbName, dbPort)
    new SqlServerContainer(containerName, db)
  }
}
