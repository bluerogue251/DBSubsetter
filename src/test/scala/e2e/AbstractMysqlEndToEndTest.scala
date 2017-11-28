package e2e

import e2e.missingfk.Tables
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.sys.process._

abstract class AbstractMysqlEndToEndTest extends AbstractEndToEndTest {
  def dataSetName: String

  override lazy val originConnString = s"jdbc:mysql://localhost:$originPort/$dataSetName?user=root"

  override lazy val targetSingleThreadedConnString = s"jdbc:mysql://0.0.0.0:$targetSingleThreadedPort/$dataSetName?user=root"

  override lazy val targetAkkaStreamsConnString = s"jdbc:mysql://0.0.0.0:$targetAkkaStreamsPort/$dataSetName?user=root"

  override def createOriginDb(): Unit = {
    val container_name = s"${dataSetName}_origin_mysql"
    s"docker rm --force --volumes $container_name".!
    s"docker create --name $container_name -p $originPort:3306 --env MYSQL_ALLOW_EMPTY_PASSWORD=true mysql:8.0".!!
    s"docker start $container_name".!!
    Thread.sleep(15000)
    s"./util/create_mysql_db.sh $dataSetName $originPort".!!
  }

  override def createSlickOriginDbConnection() = {
    slick.jdbc.MySQLProfile.backend.Database.forURL(singleThreadedConfig.originDbConnectionString)
  }

  override def createOriginDbDdl(): Unit = {
    val fut = originDb.run(DBIO.seq(Tables.schema.create))
    Await.result(fut, Duration.Inf)
  }

  override def setupTargetDbs(): Unit = {
    setupTargetDbDockerContainer("sith", targetSingleThreadedPort)
    setupTargetDbDockerContainer("akst", targetAkkaStreamsPort)
  }

  override def postSubset(): Unit = {}

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    targetSingleThreadedConn.createStatement().executeQuery("set session sql_mode = ANSI_QUOTES")
    targetAkkaStreamsConn.createStatement().executeQuery("set session sql_mode = ANSI_QUOTES")
  }

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
