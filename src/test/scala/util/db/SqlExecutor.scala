package util.db

import java.sql.{Connection, DriverManager}

object SqlExecutor {

  def execute(db: Database, sql: String): Unit = {
    val connection: Connection = DriverManager.getConnection(db.connectionString)
    connection.createStatement().execute(sql)
    connection.close()
  }
}
