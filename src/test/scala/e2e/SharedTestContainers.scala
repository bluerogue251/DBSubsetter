package e2e

import util.Ports
import util.db._

import scala.util.Properties

object SharedTestContainers {
  private val dbName = "do-not-use"

  lazy val postgres: PostgreSQLContainer = {
    val containerName = "placeholder-do-not-use"
    val port = Ports.sharedPostgresPort
    val dbHost: String = Properties.envOrElse("DB_SUBSETTER_POSTGRES_HOST", "localhost")
    val db = new PostgreSQLDatabase(dbHost, port, dbName)
    new PostgreSQLContainer(containerName, db)
  }

  lazy val sqlServer: SqlServerContainer = {
    val containerName = "placeholder-do-not-use"
    val dbHost: String = Properties.envOrElse("DB_SUBSETTER_SQL_SERVER_HOST", "localhost")
    val port = Ports.sharedSqlServerPort
    val db = new SqlServerDatabase(dbHost, dbName, port)
    new SqlServerContainer(containerName, db)
  }
}
