package trw.dbsubsetter.db

import java.sql.{Connection, DriverManager}

import scala.collection.mutable

/*
 * CAREFUL: NOT THREADSAFE
 */
class ConnectionFactory {

  /*
   * Records all open connections so that we can remember to call `close()` on them when we are finished
   */
  private[this] val registry: mutable.Set[Connection] = mutable.Set.empty[Connection]

  def closeAllConnections(): Unit = {
    registry.foreach(_.close())
  }

  def getReadOnlyConnection(connectionString: String): Connection = {
    val conn: Connection = createAndRegisterConnection(connectionString)
    conn.setReadOnly(true)
    conn
  }

  def getConnection(connectionString: String): Connection = {
    val conn: Connection = createAndRegisterConnection(connectionString)
    if (conn.isMysql) conn.createStatement().execute("SET SESSION FOREIGN_KEY_CHECKS = 0")
    conn
  }

  private[this] def createAndRegisterConnection(connectionString: String): Connection = {
    val conn: Connection = DriverManager.getConnection(connectionString)
    if (conn.isMysql) conn.createStatement().execute("SET SESSION SQL_MODE = ANSI_QUOTES")
    conn
  }
}
