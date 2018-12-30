package e2e

import util.db._
import util.docker.ContainerUtil

object SharedTestContainers {
  private val dbName = "do-not-use"

  lazy val postgres: DatabaseContainer[PostgreSQLDatabase] = {
    val containerName = "e2e_postgres"
    val port = 5497
    DatabaseContainer.startPostgreSQL(containerName, port)
    val db = new PostgreSQLDatabase(dbName, port)

    /*
     * Remove container on JVM shutdown
     */
    sys.addShutdownHook(ContainerUtil.rm(containerName))

    new PostgreSQLContainer(containerName, db)
  }

  lazy val sqlServer: DatabaseContainer[SqlServerDatabase] = {
    val containerName = "e2e_sql_server"
    val port = 5499
    DatabaseContainer.startSqlServer(containerName, port)
    val db = new SqlServerDatabase(dbName, port)

    /*
     * Remove container on JVM shutdown
     */
    sys.addShutdownHook(ContainerUtil.rm(containerName))

    new SqlServerContainer(containerName, db)
  }
}
