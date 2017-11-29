package e2e

import scala.sys.process._

abstract class AbstractPostgresqlEndToEndTest extends AbstractEndToEndTest {
  def dataSetName: String

  override def makeConnStr(p: Int): String = s"jdbc:postgresql://0.0.0.0:$p/$dataSetName?user=postgres"

  override def createOriginDb(): Unit = {
    val container_name = s"${dataSetName}_origin_postgres"
    s"docker rm --force --volumes $container_name".!
    s"docker create --name $container_name -p $originPort:5432 postgres:9.6.3".!!
    s"docker start $container_name".!!
    Thread.sleep(5000)
    s"createdb --port $originPort --host 0.0.0.0 --user postgres $dataSetName".!!
  }

  override def setupTargetDbs(): Unit = {
    setupTargetDbDockerContainer("sith", targetSingleThreadedPort)
    setupTargetDbDockerContainer("akst", targetAkkaStreamsPort)
  }

  override def postSubset(): Unit = {}

  private def setupTargetDbDockerContainer(targetType: String, port: Int): Unit = {
    val containerName = s"${dataSetName}_target_${targetType}_postgres"
    s"docker rm --force --volumes $containerName".!
    s"docker create --name $containerName -p $port:5432 postgres:9.6.3".!!
    s"docker start $containerName".!!
    Thread.sleep(5000)
    s"createdb --port $port --host 0.0.0.0 --user postgres $dataSetName".!!
    s"./util/sync_postgres_origin_to_target.sh $dataSetName $originPort $port".!!
  }
}
