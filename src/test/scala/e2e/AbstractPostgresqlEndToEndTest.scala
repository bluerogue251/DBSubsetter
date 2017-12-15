package e2e

import scala.sys.process._

abstract class AbstractPostgresqlEndToEndTest extends AbstractEndToEndTest {
  override val profile = slick.jdbc.PostgresProfile

  def dataSetName: String

  override def makeConnStr(p: Int, dbName: String): String = s"jdbc:postgresql://0.0.0.0:$p/$dataSetName?user=postgres"

  override def createOriginDb(): Unit = {
    setupDockerContainer(s"${dataSetName}_origin_postgres", originPort)
  }

  override def setupTargetDbs(): Unit = {
    setupDockerContainer(s"${dataSetName}_target_sith_postgres", targetSingleThreadedPort)
    setupDockerContainer(s"${dataSetName}_target_akst_postgres", targetAkkaStreamsPort)
    s"./src/test/util/sync_postgres_origin_to_target.sh $dataSetName $originPort $targetSingleThreadedPort".!!
    s"./src/test/util/sync_postgres_origin_to_target.sh $dataSetName $originPort $targetAkkaStreamsPort".!!
  }

  override def postSubset(): Unit = {
    s"./src/test/util/postgres_post_subset.sh $dataSetName $originPort $targetSingleThreadedPort".!!
    s"./src/test/util/postgres_post_subset.sh $dataSetName $originPort $targetAkkaStreamsPort".!!
  }

  private def setupDockerContainer(containerName: String, port: Int): Unit = {
    removeDockerContainer(containerName)
    s"docker create --name $containerName -p $port:5432 postgres:9.6.3".!!
    s"docker start $containerName".!!
    Thread.sleep(10000)
    s"createdb --port $port --host 0.0.0.0 --user postgres $dataSetName".!!
  }
}
