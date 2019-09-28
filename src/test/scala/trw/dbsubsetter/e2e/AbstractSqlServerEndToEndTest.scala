package trw.dbsubsetter.e2e

import trw.dbsubsetter.util.db._

import scala.sys.process._

abstract class AbstractSqlServerEndToEndTest extends AbstractEndToEndTest[SqlServerDatabase] {
  override protected val profile = slick.jdbc.SQLServerProfile

  protected def testName: String

  override protected def startOriginContainer():Unit = SharedTestContainers.sqlServer

  override protected def startTargetContainers(): Unit = {} // No-op (container is shared with origin)

  override protected def awaitContainersReady(): Unit = SharedTestContainers.awaitSqlServerUp

  override protected def createOriginDatabase(): Unit = {
    createEmptyDb(containers.origin.name, containers.origin.db.name)
  }

  override protected def createTargetDatabases(): Unit = {
    createEmptyDb(containers.origin.name, containers.targetSingleThreaded.db.name)
    createEmptyDb(containers.origin.name, containers.targetAkkaStreams.db.name)
  }

  override protected def containers: DatabaseContainerSet[SqlServerDatabase] = {
    val containerName = SharedTestContainers.sqlServer.name
    val port = SharedTestContainers.sqlServer.db.port
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
    s"./src/test/trw.dbsubsetter.util/sync_sqlserver_origin_to_target.sh ${containers.origin.name} ${containers.origin.db.name} ${containers.targetSingleThreaded.db.name}".!!
    s"./src/test/trw.dbsubsetter.util/sync_sqlserver_origin_to_target.sh ${containers.origin.name} ${containers.origin.db.name} ${containers.targetAkkaStreams.db.name}".!!
  }

  override protected def postSubset(): Unit = {
    s"./src/test/trw.dbsubsetter.util/sqlserver_post_subset.sh ${containers.origin.name} ${containers.targetSingleThreaded.db.name}".!!
    s"./src/test/trw.dbsubsetter.util/sqlserver_post_subset.sh ${containers.origin.name} ${containers.targetAkkaStreams.db.name}".!!
  }

  private def createEmptyDb(containerName: String, dbName: String): Unit = {
    s"./src/test/trw.dbsubsetter.util/create_sqlserver_db.sh $containerName $dbName".!!
  }

  private def buildContainer(containerName: String, dbName: String, dbPort: Int): SqlServerContainer = {
    val db: SqlServerDatabase = new SqlServerDatabase(dbName, dbPort)
    new SqlServerContainer(containerName, db)
  }
}