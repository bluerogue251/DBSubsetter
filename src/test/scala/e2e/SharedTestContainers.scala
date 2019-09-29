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

  lazy val awaitPostgresUp: Unit = Thread.sleep(5000)

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

  lazy val mysqlOrigin: DatabaseContainer[MySqlDatabase] = startMysql("e2e_mysql_origin", Ports.sharedMySqlOriginPort)
  lazy val mysqlTargetSingleThreaded: DatabaseContainer[MySqlDatabase] = startMysql("e2e_mysql_target_single_threaded", Ports.sharedMySqlTargetSingleThreadedPort)
  lazy val mysqlTargetAkkaStreams: DatabaseContainer[MySqlDatabase] = startMysql("e2e_mysql_target_akka_streams", Ports.sharedMySqlTargetAkkaStreamsPort)

  lazy val awaitMysqlUp: Unit = Thread.sleep(15000)

  private def startMysql(containerName: String, port: Int): MySqlContainer = {
    DatabaseContainer.recreateMySql(containerName, port)
    val db = new MySqlDatabase(dbName, port)

    /*
     * Remove container on JVM shutdown
     */
    sys.addShutdownHook(ContainerUtil.rm(containerName))

    new MySqlContainer(containerName, db)
  }
}
