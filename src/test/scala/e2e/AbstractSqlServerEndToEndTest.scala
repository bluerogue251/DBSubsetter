package e2e

import scala.sys.process._

abstract class AbstractSqlServerEndToEndTest extends AbstractEndToEndTest {
  override val profile = slick.jdbc.SQLServerProfile

  override lazy val targetSingleThreadedPort: Int = originPort
  override lazy val targetAkkaStreamsPort: Int = originPort
  override lazy val targetSingleThreadedDbName = s"${dataSetName}_sith"
  override lazy val targetAkkaStreamsDbName = s"${dataSetName}_akst"
  lazy val containerName = s"${dataSetName}_sqlserver"

  override def makeConnStr(port: Int, dbName: String): String = s"jdbc:sqlserver://localhost:$port;databaseName=$dbName;user=sa;password=MsSqlServerLocal1"

  override def createOriginDb(): Unit = {
    s"docker rm --force --volumes $containerName".!
    s"docker create --name $containerName -p $originPort:1433 --env ACCEPT_EULA=Y --env SA_PASSWORD=MsSqlServerLocal1 --env MSSQL_PID=Developer microsoft/mssql-server-linux:2017-CU2".!!
    s"docker start $containerName".!!
    Thread.sleep(20000)
    s"./src/test/util/create_sqlserver_db.sh $containerName $dataSetName".!!
  }

  override def setupTargetDbs(): Unit = {
    s"./src/test/util/sync_sqlserver_origin_to_target.sh $containerName $dataSetName $targetSingleThreadedDbName".!!
    s"./src/test/util/sync_sqlserver_origin_to_target.sh $containerName $dataSetName $targetAkkaStreamsDbName".!!
  }

  override def postSubset(): Unit = {
    s"./src/test/util/sqlserver_post_subset.sh $containerName $targetSingleThreadedDbName".!!
    s"./src/test/util/sqlserver_post_subset.sh $containerName $targetAkkaStreamsDbName".!!
  }
}