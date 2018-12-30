package e2e

import util.db._
import util.docker.ContainerUtil

object SharedTestContainers {
  lazy val postgres: DatabaseContainer[PostgreSQLDatabase] = {
    val containerName = "e2e_postgres"
    val dbName = "postgres"
    val port = 5497
    DatabaseContainer.startPostgreSQL(containerName, port)
    val db = new PostgreSQLDatabase(dbName, port)

    /*
     * Remove container on JVM shutdown
     */
    sys.addShutdownHook(ContainerUtil.rm(containerName))

    new PostgreSQLContainer(containerName, db)
  }

  lazy val mysql: DatabaseContainer[MySqlDatabase] = {
    val containerName = "e2e_mysql"
    val dbName = "mysql"
    val port = 5498
    DatabaseContainer.startMySql(containerName, port)
    val db = new MySqlDatabase(dbName, port)

    /*
     * Remove container on JVM shutdown
     */
    sys.addShutdownHook(ContainerUtil.rm(containerName))

    new MySqlContainer(containerName, db)
  }
}
