package e2e

import util.db._

import scala.sys.process._

abstract class AbstractSqlServerEndToEndTest extends AbstractEndToEndTest[SqlServerDatabase] {
  override protected val profile = slick.jdbc.SQLServerProfile

  protected def testName: String

  protected def port: Int

  override protected def startContainers(): DatabaseContainerSet[SqlServerDatabase] = {
    val containerName = s"${testName}_sqlserver"
    DatabaseContainer.startSqlServer(containerName, port)
    Thread.sleep(6000)

    val originDbName = s"${testName}_origin"
    val targetSingleThreadedDbName = s"${testName}_target_single_threaded"
    val targetAkkaStreamsDbName = s"${testName}_target_akka_streams"

    val originContainer = buildContainer(containerName, originDbName, port)
    val targetSingleThreadedContainer = buildContainer(containerName, targetSingleThreadedDbName, port)
    val targetAkkaStreamsContainer = buildContainer(containerName, targetAkkaStreamsDbName, port)

    new DatabaseContainerSet(originContainer, targetSingleThreadedContainer, targetAkkaStreamsContainer)
  }

  override protected def createEmptyDatabases(): Unit = {
    s"./src/test/util/create_sqlserver_db.sh ${containers.origin.name} ${containers.origin.db.name}".!!
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

  private def buildContainer(containerName: String, dbName: String, dbPort: Int): SqlServerContainer = {
    val db: SqlServerDatabase = new SqlServerDatabase(dbName, dbPort)
    new SqlServerContainer(containerName, db)
  }

}
