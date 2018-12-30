package e2e

import util.db._

object SharedPostgreSQLContainer {
  lazy val container: DatabaseContainer[PostgreSQLDatabase] = {
    val containerName = "e2e_postgres"
    val dbName = "postgres"
    val port = 5499
    DatabaseContainer.startPostgreSQL(containerName, port)
    val db = new PostgreSQLDatabase(dbName, port)
    new PostgreSQLContainer(containerName, db)
  }
}
