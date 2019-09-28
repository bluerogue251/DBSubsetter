package trw.dbsubsetter.util.db

import trw.dbsubsetter.util.docker.ContainerUtil

import scala.sys.process._

trait DatabaseContainer[T <: Database] {
  def name: String
  def db: T
}

object DatabaseContainer {
  def recreateMySql(name: String, port: Int): Unit = {
    ContainerUtil.rm(name)
    s"docker create --name $name -p $port:3306 --env MYSQL_ALLOW_EMPTY_PASSWORD=true mysql:8.0.3".!!
    ContainerUtil.start(name)
  }

  def recreatePostgreSQL(name: String, port: Int): Unit = {
    ContainerUtil.rm(name)
    s"docker create --name $name -p $port:5432 postgres:9.6.3".!!
    ContainerUtil.start(name)
  }

  def recreateSqlServer(name: String, port: Int): Unit = {
    ContainerUtil.rm(name)
    s"docker create --name $name -p $port:1433 --env ACCEPT_EULA=Y --env SA_PASSWORD=MsSqlServerLocal1 --env MSSQL_PID=Developer microsoft/mssql-server-linux:2017-CU12 /opt/mssql/bin/sqlservr".!!
    ContainerUtil.start(name)
  }
}

class MySqlContainer(val name: String, val db: MySqlDatabase) extends DatabaseContainer[MySqlDatabase]
class PostgreSQLContainer(val name: String, val db: PostgreSQLDatabase) extends DatabaseContainer[PostgreSQLDatabase]
class SqlServerContainer(val name: String, val db: SqlServerDatabase) extends DatabaseContainer[SqlServerDatabase]