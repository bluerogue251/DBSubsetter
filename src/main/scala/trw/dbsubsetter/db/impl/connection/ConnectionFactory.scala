package trw.dbsubsetter.db.impl.connection

import java.sql.{Connection, DriverManager}

import scala.collection.mutable

/*
 * CAREFUL: NOT THREADSAFE
 */
private[db] class ConnectionFactory {

  /*
   * Records all open connections so that we can remember to call `close()` on them when we are finished
   */
  private[this] val registry: mutable.Set[Connection] = mutable.Set.empty[Connection]

  def closeAllConnections(): Unit = {
    registry.foreach(_.close())
  }

  def getReadOnlyConnection(connectionString: String): Connection = {
    val connection: Connection = createAndRegisterConnection(connectionString)
    connection.setReadOnly(true)
    connection
  }

  def getConnectionWithWritePrivileges(connectionString: String): Connection = {
    val connection: Connection = createAndRegisterConnection(connectionString)
    import trw.dbsubsetter.db._
    if (connection.isMysql) connection.createStatement().execute("SET SESSION FOREIGN_KEY_CHECKS = 0")
    connection
  }

  private[this] def createAndRegisterConnection(connectionString: String): Connection = {
    val connection: Connection = DriverManager.getConnection(connectionString)
    import trw.dbsubsetter.db._
    if (connection.isMysql) connection.createStatement().execute("SET SESSION SQL_MODE = ANSI_QUOTES")
    registry.add(connection)
    connection
  }
}
