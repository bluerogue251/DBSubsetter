package e2e

import e2e.ddl.Tables
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.sys.process._

abstract class AbstractMysqlEndToEndTest extends AbstractEndToEndTest {
  override val dbVendor = "MySQL"
  override val dockerImage = "mysql:8.0"
  override val originConnString = s"jdbc:mysql://localhost:$originPort/$dataSetName?user=root"

  override val targetSingleThreadedConnString = s"jdbc:mysql://0.0.0.0:$targetSingleThreadedPort/$dataSetName?user=root"

  override val targetAkkaStreamsConnString = s"jdbc:mysql://0.0.0.0:$targetAkkaStreamsPort/$dataSetName?user=root"

  override def createOriginDbDockerContainer(): Unit = {
    val container_name = s"${dataSetName}_origin_mysql"
    s"docker rm --force --volumes $container_name".!
    s"docker create --name $container_name -p $originPort:3306 --env MYSQL_ALLOW_EMPTY_PASSWORD=true mysql:8.0".!!
    s"docker start $container_name".!!
    Thread.sleep(15000)
  }

  override def createSlickOriginDbConnection() = {
    slick.jdbc.MySQLProfile.backend.Database.forURL(singleThreadedConfig.originDbConnectionString)
  }

  override def createOriginDb(): Unit = {
    s"mysql --port $originPort --host 0.0.0.0 --user root -e'create database $dataSetName'".!
  }

  def createOriginDbDdl(): Unit = {
    val fut = originDb.run(DBIO.seq(Tables.schema.create))
    Await.result(fut, Duration.Inf)
  }

  def setupTargetDbs(): Unit = {
    setupTargetDbDockerContainer("sith", targetSingleThreadedPort)
    setupTargetDbDockerContainer("akst", targetAkkaStreamsPort)
  }

  private def setupTargetDbDockerContainer(targetType: String, port: Int): Unit = {
    val containerName = s"${dataSetName}_target_${targetType}_mysql"
    s"docker rm --force --volumes $containerName".!
    s"docker create --name $containerName --port $port:3306 --env MYSQL_ALLOW_EMPTY_PASSWORD=true mysql:8.0".!!
    s"docker start $containerName".!!
    Thread.sleep(15000)
    s"mysql --port $port --host 0.0.0.0 --user root -e 'create database $dataSetName".!!
    s"mysqldump --host 0.0.0.0 --port $originPort --user root --no-data $dataSetName | mysql --host 0.0.0.0 --port $port --user root $dataSetName".!!
  }
}
