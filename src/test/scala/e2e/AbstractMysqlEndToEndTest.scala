package e2e

import util.docker.ContainerUtil

import scala.sys.process._

abstract class AbstractMysqlEndToEndTest extends AbstractEndToEndTest {
  override val profile = slick.jdbc.MySQLProfile

  def dataSetName: String

  def originContainerName = s"${dataSetName}_origin_mysql"

  def targetSithContainerName = s"${dataSetName}_target_sith_mysql"

  def targetAkstContainerName = s"${dataSetName}_target_akst_mysql"

  override def makeConnStr(port: Int, dbName: String): String = s"jdbc:mysql://localhost:$port/$dataSetName?user=root&useSSL=false&rewriteBatchedStatements=true"

  override def prepareOriginDb(): Unit = createMySqlDatabase(originContainerName)

  override def prepareTargetDbs(): Unit = {
    createMySqlDatabase(targetSithContainerName)
    createMySqlDatabase(targetAkstContainerName)
    s"./src/test/util/sync_mysql_origin_to_target.sh $dataSetName $originContainerName $targetSithContainerName".!!
    s"./src/test/util/sync_mysql_origin_to_target.sh $dataSetName $originContainerName $targetAkstContainerName".!!
  }

  override def postSubset(): Unit = {} // No-op

  override protected def createContainers(): Unit = {
    def createAndStart(name: String, port: Int): Unit = {
      ContainerUtil.rm(name)
      s"docker create --name $name -p $port:3306 --env MYSQL_ALLOW_EMPTY_PASSWORD=true mysql:8.0.3".!!
      ContainerUtil.start(name)
    }

    createAndStart(originContainerName, originPort)
    createAndStart(targetSithContainerName, targetSingleThreadedPort)
    createAndStart(targetAkstContainerName, targetAkkaStreamsPort)
    Thread.sleep(13000)
  }

  private def createMySqlDatabase(container: String): Unit = s"./src/test/util/create_mysql_db.sh $dataSetName $container".!!

  override protected def afterAll(): Unit = {
    super.afterAll()
    ContainerUtil.rm(originContainerName)
    ContainerUtil.rm(targetSithContainerName)
    ContainerUtil.rm(targetAkstContainerName)
  }
}
