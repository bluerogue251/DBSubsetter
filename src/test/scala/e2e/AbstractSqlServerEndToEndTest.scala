package e2e

import util.db._

import scala.sys.process._

abstract class AbstractSqlServerEndToEndTest extends AbstractEndToEndTest[SqlServerDatabase] {
  override protected val profile = slick.jdbc.SQLServerProfile

  protected def testName: String

  override protected def startOriginContainer():Unit = {} // No-op

  override protected def startTargetContainers(): Unit = {} // No-op (container is shared with origin)

  override protected def awaitContainersReady(): Unit = {} // No-op

  override protected def createOriginDatabase(): Unit = {
    createEmptyDb(containers.origin.name, containers.origin.db.name)
  }

  override protected def createTargetDatabases(): Unit = {
    createEmptyDb(containers.origin.name, containers.targetSingleThreaded.db.name)
    createEmptyDb(containers.origin.name, containers.targetAkkaStreams.db.name)
  }

  override protected def containers: DatabaseContainerSet[SqlServerDatabase] = {
    val containerName = SharedTestContainers.sqlServer.name
    val host = SharedTestContainers.sqlServer.db.host
    val port = SharedTestContainers.sqlServer.db.port
    val originDbName = s"${testName}_origin"
    val targetSingleThreadedDbName = s"${testName}_target_single_threaded"
    val targetAkkaStreamsDbName = s"${testName}_target_akka_streams"

    new DatabaseContainerSet(
      buildContainer(host, originDbName, port),
      buildContainer(host, targetSingleThreadedDbName, port),
      buildContainer(host, targetAkkaStreamsDbName, port)
    )
  }

  override protected def prepareOriginDDL(): Unit

  override protected def prepareOriginDML(): Unit

  override protected def prepareTargetDDL(): Unit = {
    s"./src/test/util/sync_sqlserver_origin_to_target.sh ${containers.origin.db.host} ${containers.origin.db.name} ${containers.targetSingleThreaded.db.name}".!!
    s"./src/test/util/sync_sqlserver_origin_to_target.sh ${containers.origin.db.host} ${containers.origin.db.name} ${containers.targetAkkaStreams.db.name}".!!
  }

  override protected def postSubset(): Unit = {
    s"./src/test/util/sqlserver_post_subset.sh ${containers.origin.db.host} ${containers.targetSingleThreaded.db.name}".!!
    s"./src/test/util/sqlserver_post_subset.sh ${containers.origin.db.host} ${containers.targetAkkaStreams.db.name}".!!
  }

  private def createEmptyDb(containerName: String, dbName: String): Unit = {
    s"./src/test/util/create_sqlserver_db.sh ${containers.origin.db.host} $dbName".!!
  }

  private def buildContainer(dbHost: String, dbName: String, dbPort: Int): SqlServerContainer = {
    val db: SqlServerDatabase = new SqlServerDatabase(dbHost, dbName, dbPort)
    new SqlServerContainer(SharedTestContainers.sqlServer.name, db)
  }
}
