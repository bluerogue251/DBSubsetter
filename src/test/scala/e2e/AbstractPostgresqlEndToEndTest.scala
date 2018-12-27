package e2e

import scala.sys.process._

abstract class AbstractPostgresqlEndToEndTest extends AbstractEndToEndTest {
  override val profile = slick.jdbc.PostgresProfile

  def dataSetName: String

  protected def originContainerName = s"${dataSetName}_origin_postgres"

  private def targetSingleThreadedContainerName = s"${dataSetName}_target_sith_postgres"

  private def targetAkkaStreamsContainerName = s"${dataSetName}_target_akst_postgres"

  override def makeConnStr(p: Int, dbName: String): String = s"jdbc:postgresql://0.0.0.0:$p/$dataSetName?user=postgres"

  override def setupOriginDb(): Unit = createDb(originContainerName)

  override def setupTargetDbs(): Unit = {
    createDb(targetSingleThreadedContainerName)
    createDb(targetAkkaStreamsContainerName)
    s"./src/test/util/sync_postgres_origin_to_target.sh $dataSetName $originContainerName $targetSingleThreadedContainerName".!!
    s"./src/test/util/sync_postgres_origin_to_target.sh $dataSetName $originContainerName $targetAkkaStreamsContainerName".!!
  }

  override def postSubset(): Unit = {
    s"./src/test/util/postgres_post_subset.sh $dataSetName $originContainerName $targetSingleThreadedContainerName".!!
    s"./src/test/util/postgres_post_subset.sh $dataSetName $originContainerName $targetAkkaStreamsContainerName".!!
  }

  override protected def createDockerContainers(): Unit = {
    def createAndStart(name: String, port: Int): Unit = {
      dockerRm(name)
      s"docker create --name $name -p $port:5432 postgres:9.6.3".!!
      dockerStart(name)
    }

    createAndStart(originContainerName, originPort)
    createAndStart(targetSingleThreadedContainerName, targetSingleThreadedPort)
    createAndStart(targetAkkaStreamsContainerName, targetAkkaStreamsPort)
    Thread.sleep(5000)
  }

  private def createDb(dockerContainer: String): Unit = {
    s"docker exec $dockerContainer createdb --user postgres $dataSetName".!!
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    dockerRm(targetSingleThreadedContainerName)
    dockerRm(targetAkkaStreamsContainerName)
  }
}
