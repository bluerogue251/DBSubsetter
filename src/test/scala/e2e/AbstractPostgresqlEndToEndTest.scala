package e2e

import scala.sys.process._

abstract class AbstractPostgresqlEndToEndTest extends AbstractEndToEndTest {
  override val profile = slick.jdbc.PostgresProfile

  def dataSetName: String

  override def makeConnStr(p: Int, dbName: String): String = s"jdbc:postgresql://0.0.0.0:$p/$dataSetName?user=postgres"

  override def setupOriginDb(): Unit = if (recreateOriginDB) createDb(originPort)

  override def setupTargetDbs(): Unit = {
    createDb(targetSingleThreadedPort)
    createDb(targetAkkaStreamsPort)
    s"./src/test/util/sync_postgres_origin_to_target.sh $dataSetName $originPort $targetSingleThreadedPort".!!
    s"./src/test/util/sync_postgres_origin_to_target.sh $dataSetName $originPort $targetAkkaStreamsPort".!!
  }

  override def postSubset(): Unit = {
    s"./src/test/util/postgres_post_subset.sh $dataSetName $originPort $targetSingleThreadedPort".!!
    s"./src/test/util/postgres_post_subset.sh $dataSetName $originPort $targetAkkaStreamsPort".!!
  }

  override protected def createDockerContainers(): Unit = {
    def createAndStart(name: String, port: Int): Unit = {
      dockerRm(name)
      s"docker create --name $name -p $port:5432 postgres:9.6.3".!!
      dockerStart(name)
    }

    val originContainerName = s"${dataSetName}_origin_postgres"
    if (recreateOriginDB) createAndStart(originContainerName, originPort) else dockerStart(originContainerName)
    createAndStart(s"${dataSetName}_target_sith_postgres", targetSingleThreadedPort)
    createAndStart(s"${dataSetName}_target_akst_postgres", targetAkkaStreamsPort)
    Thread.sleep(5000)
  }

  private def createDb(port: Int): Unit = {
    s"createdb --port $port --host 0.0.0.0 --user postgres $dataSetName".!!
  }
}
