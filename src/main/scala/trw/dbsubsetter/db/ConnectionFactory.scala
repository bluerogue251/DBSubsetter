package trw.dbsubsetter.db

import java.sql.{Connection, DriverManager}

import scala.collection.mutable


/**
  * WARNING: this class is not threadsafe
  */
final class ConnectionFactory {

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

  def getReadWriteConnection(connectionString: String): Connection = {
    val connection: Connection = createAndRegisterConnection(connectionString)
    if (connection.isMysql) connection.createStatement().execute("SET SESSION FOREIGN_KEY_CHECKS = 0")
    connection
  }

  // TODO refactor
  def getDbVendor(connectionString: String): DbVendor = {
    val connection: Connection = createAndRegisterConnection(connectionString)
    val vendor: DbVendor = connection.dbVendor
    registry.remove(connection)
    connection.close()
    vendor
  }

  private[this] def createAndRegisterConnection(connectionString: String): Connection = {
    val connection: Connection = DriverManager.getConnection(connectionString)
    if (connection.isMysql) connection.createStatement().execute("SET SESSION SQL_MODE = ANSI_QUOTES")
    registry.add(connection)
    connection
  }
}
