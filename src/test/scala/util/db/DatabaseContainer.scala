package util.db

import util.docker.{Container, ContainerUtil}

import scala.sys.process._

class DatabaseContainer[T <: Database](container: Container[T]) {
  def name: String = container.name
  def db: T = container.process
}

object DatabaseContainer {
  def startMySql(name: String, port: Int): Unit = {
    ContainerUtil.rm(name)
    s"docker create --name $name -p $port:3306 --env MYSQL_ALLOW_EMPTY_PASSWORD=true mysql:8.0.3".!!
    ContainerUtil.start(name)
  }

  def startPostgreSQL(name: String, port: Int): Unit = {
    ContainerUtil.rm(name)
    s"docker create --name $name -p $port:5432 postgres:9.6.3".!!
    ContainerUtil.start(name)
  }

  def startSqlServer(name: String, port: Int): Unit = {
    ContainerUtil.rm(name)
    s"docker create --name $name -p $port:1433 --env ACCEPT_EULA=Y --env SA_PASSWORD=MsSqlServerLocal1 --env MSSQL_PID=Developer microsoft/mssql-server-linux:2017-CU12 /opt/mssql/bin/sqlservr".!!
    ContainerUtil.start(name)
  }
}