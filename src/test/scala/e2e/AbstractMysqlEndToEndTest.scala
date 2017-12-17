package e2e

import scala.sys.process._

abstract class AbstractMysqlEndToEndTest extends AbstractEndToEndTest {
  override val profile = slick.jdbc.MySQLProfile

  def dataSetName: String

  private lazy val targetSithContainerName = s"${dataSetName}_target_sith_mysql"
  private lazy val targetAkstContainerName = s"${dataSetName}_target_akst_mysql"

  override def makeConnStr(port: Int, dbName: String): String = s"jdbc:mysql://localhost:$port/$dataSetName?user=root&useSSL=false&rewriteBatchedStatements=true"

  override def createOriginDb(): Unit = {
    setupDockerContainer(s"${dataSetName}_origin_mysql", originPort)
  }

  override def setupTargetDbs(): Unit = {
    setupDockerContainer(targetSithContainerName, targetSingleThreadedPort)
    setupDockerContainer(targetAkstContainerName, targetAkkaStreamsPort)

    s"./src/test/util/sync_mysql_origin_to_target.sh $dataSetName $originPort $targetSingleThreadedPort".!!
    s"./src/test/util/sync_mysql_origin_to_target.sh $dataSetName $originPort $targetAkkaStreamsPort".!!
  }

  override def postSubset(): Unit = {} // No-op

  private def setupDockerContainer(containerName: String, port: Int): Unit = {
    removeDockerContainer(containerName)
    s"docker create --name $containerName -p $port:3306 --env MYSQL_ALLOW_EMPTY_PASSWORD=true mysql:8.0".!!
    s"docker start $containerName".!!
    Thread.sleep(20000)
    s"./src/test/util/create_mysql_db.sh $dataSetName $port".!!
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    //    removeDockerContainer(targetSithContainerName)
    //    removeDockerContainer(targetAkstContainerName)
  }
}
