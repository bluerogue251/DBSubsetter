package e2e

import scala.sys.process._

abstract class AbstractMysqlEndToEndTest extends AbstractEndToEndTest {
  def dataSetName: String

  override def makeConnStr(port: Int): String = s"jdbc:mysql://localhost:$port/$dataSetName?user=root"

  override def createOriginDb(): Unit = {
    val container_name = s"${dataSetName}_origin_mysql"
    s"docker rm --force --volumes $container_name".!
    s"docker create --name $container_name -p $originPort:3306 --env MYSQL_ALLOW_EMPTY_PASSWORD=true mysql:8.0".!!
    s"docker start $container_name".!!
    Thread.sleep(15000)
    s"./util/create_mysql_db.sh $dataSetName $originPort".!!
  }

  override def setupTargetDbs(): Unit = {
    setupTargetDbDockerContainer("sith", targetSingleThreadedPort)
    setupTargetDbDockerContainer("akst", targetAkkaStreamsPort)
  }

  override def postSubset(): Unit = {}

  private def setupTargetDbDockerContainer(targetType: String, port: Int): Unit = {
    val containerName = s"${dataSetName}_target_${targetType}_mysql"
    s"docker rm --force --volumes $containerName".!
    s"docker create --name $containerName -p $port:3306 --env MYSQL_ALLOW_EMPTY_PASSWORD=true mysql:8.0".!!
    s"docker start $containerName".!!
    Thread.sleep(15000)
    s"./util/create_mysql_db.sh $dataSetName $port".!!
    s"./util/sync_mysql_origin_to_target.sh $dataSetName $originPort $port".!!
  }
}
