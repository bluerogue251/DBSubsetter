package e2e

import util.Ports
import util.db._
import util.docker.ContainerUtil

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
    val containerName = "e2e_sql_server"
    val port = Ports.sharedSqlServerPort
    DatabaseContainer.recreateSqlServer(containerName, port)
    val db = new SqlServerDatabase(dbName, port)

    /*
     * Remove container on JVM shutdown
     */
    sys.addShutdownHook(ContainerUtil.rm(containerName))

    new SqlServerContainer(containerName, db)
  }

  lazy val awaitSqlServerUp: Unit = Thread.sleep(6000)

  lazy val mysqlOrigin: DatabaseContainer[MySqlDatabase] = startMysql(Ports.sharedMySqlOriginPort)
  lazy val mysqlTargetSingleThreaded: DatabaseContainer[MySqlDatabase] = startMysql(Ports.sharedMySqlTargetSingleThreadedPort)
  lazy val mysqlTargetAkkaStreams: DatabaseContainer[MySqlDatabase] = startMysql(Ports.sharedMySqlTargetAkkaStreamsPort)

  private def startMysql(port: Int): MySqlContainer = {
    val dbHost: String = Properties.envOrElse("DB_SUBSETTER_MYSQL_HOST", "localhost")
    val db = new MySqlDatabase(dbHost, port, dbName)
    new MySqlContainer("placholder-do-not-use", db)
  }
}
