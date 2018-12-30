package e2e

import util.db._
import util.docker.ContainerUtil

object SharedTestContainers {
  private val dbName = "do-not-use"

  lazy val postgres: PostgreSQLContainer = {
    val containerName = "e2e_postgres"
    val port = 5495
    DatabaseContainer.startPostgreSQL(containerName, port)
    val db = new PostgreSQLDatabase(dbName, port)

    /*
     * Remove container on JVM shutdown
     */
    sys.addShutdownHook(ContainerUtil.rm(containerName))

    new PostgreSQLContainer(containerName, db)
  }

  lazy val awaitPostgresUp: Unit = Thread.sleep(4000)

  lazy val sqlServer: SqlServerContainer = {
    val containerName = "e2e_sql_server"
    val port = 5496
    DatabaseContainer.startSqlServer(containerName, port)
    val db = new SqlServerDatabase(dbName, port)

    /*
     * Remove container on JVM shutdown
     */
    sys.addShutdownHook(ContainerUtil.rm(containerName))

    new SqlServerContainer(containerName, db)
  }

  lazy val awaitSqlServerUp: Unit = Thread.sleep(5000)

  lazy val mysqlOrigin: DatabaseContainer[MySqlDatabase] = startMysql("e2e_mysql_origin", 5497)
  lazy val mysqlTargetSingleThreaded: DatabaseContainer[MySqlDatabase] = startMysql("e2e_mysql_target_single_threaded", 5498)
  lazy val mysqlTargetAkkaStreams: DatabaseContainer[MySqlDatabase] = startMysql("e2e_mysql_target_akka_streams", 5499)

  lazy val awaitMysqlUp: Unit = Thread.sleep(13000)

  private def startMysql(containerName: String, port: Int): MySqlContainer = {
    DatabaseContainer.startMySql(containerName, port)
    val db = new MySqlDatabase(dbName, port)

    /*
     * Remove container on JVM shutdown
     */
    sys.addShutdownHook(ContainerUtil.rm(containerName))

    new MySqlContainer(containerName, db)
  }
}
