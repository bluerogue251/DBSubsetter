package e2e

import util.docker.ContainerUtil

import scala.sys.process._

abstract class AbstractSqlServerEndToEndTest extends AbstractEndToEndTest {
  override val profile = slick.jdbc.SQLServerProfile

  override def prepareOriginDb(): Unit = {
    s"./src/test/util/create_sqlserver_db.sh ${containers.origin.name} ${containers.origin.db.name}".!!
  }

  override def prepareTargetDbs(): Unit = {
    s"./src/test/util/sync_sqlserver_origin_to_target.sh $containerName $dataSetName $targetSingleThreadedDbName".!!
    s"./src/test/util/sync_sqlserver_origin_to_target.sh $containerName $dataSetName $targetAkkaStreamsDbName".!!
  }

  override def postSubset(): Unit = {
    s"./src/test/util/sqlserver_post_subset.sh $containerName $targetSingleThreadedDbName".!!
    s"./src/test/util/sqlserver_post_subset.sh $containerName $targetAkkaStreamsDbName".!!
  }

  override protected def createContainers(): Unit = {
    ContainerUtil.rm(containerName)
    s"docker create --name $containerName -p $originPort:1433 --env ACCEPT_EULA=Y --env SA_PASSWORD=MsSqlServerLocal1 --env MSSQL_PID=Developer microsoft/mssql-server-linux:2017-CU12 /opt/mssql/bin/sqlservr".!!
    ContainerUtil.start(containerName)
    Thread.sleep(6000)
  }
}
