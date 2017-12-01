package e2e

import scala.sys.process._

abstract class AbstractSqlServerEndToEndTest extends AbstractEndToEndTest {
  override val profile = slick.jdbc.SQLServerProfile

  def dataSetName: String

  override def makeConnStr(port: Int): String = s"jdbc:sqlserver://localhost:$port;databaseName=$dataSetName;user=sa;password=MsSqlServerLocal1"

  override def createOriginDb(): Unit = {
    setupDockerContainer(s"${dataSetName}_origin_sqlserver", originPort)
  }

  override def setupTargetDbs(): Unit = {
    setupDockerContainer(s"${dataSetName}_target_sith_sqlserver", targetSingleThreadedPort)
    setupDockerContainer(s"${dataSetName}_target_akst_sqlserver", targetAkkaStreamsPort)

    s"./src/test/util/sync_sqlserver_origin_to_target.sh $dataSetName $originPort $targetSingleThreadedPort".!!
    s"./src/test/util/sync_sqlserver_origin_to_target.sh $dataSetName $originPort $targetAkkaStreamsPort".!!
  }

  override def postSubset(): Unit = {} // No-op

  private def setupDockerContainer(containerName: String, port: Int): Unit = {
    s"docker rm --force --volumes $containerName".!
    s"docker create --name $containerName -p $port:1433 --env ACCEPT_EULA=Y --env SA_PASSWORD=MsSqlServerLocal1 --env MSSQL_PID=Developer microsoft/mssql-server-linux:2017-CU2".!!
    s"docker start $containerName".!!
    Thread.sleep(10000)
    s"./src/test/util/create_sqlserver_db.sh $containerName $dataSetName".!!
  }
}
