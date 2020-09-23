package trw.dbsubsetter.db.impl

import java.sql.{Connection, DriverManager}

import trw.dbsubsetter.db.DbVendor

import scala.collection.mutable

/**
  * WARNING: this class is not threadsafe
  */
private[db] final class ConnectionFactory {

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
    import trw.dbsubsetter.db._
    if (connection.isMysql) connection.createStatement().execute("SET SESSION FOREIGN_KEY_CHECKS = 0")
    connection
  }

  def getDbVendor(connectionString: String): DbVendor = {
    val connection: Connection = createAndRegisterConnection(connectionString)
    import trw.dbsubsetter.db._
    val vendor: DbVendor = connection.dbVendor
    registry.remove(connection)
    connection.close()
    vendor
  }

  private[this] def createAndRegisterConnection(connectionString: String): Connection = {
    val connection: Connection = DriverManager.getConnection(connectionString)
    import trw.dbsubsetter.db._
    if (connection.isMysql) connection.createStatement().execute("SET SESSION SQL_MODE = ANSI_QUOTES")
    registry.add(connection)
    connection
  }
}
