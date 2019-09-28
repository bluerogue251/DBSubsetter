package trw.dbsubsetter.db.impl.workaroundtest

import java.sql.Connection

import trw.dbsubsetter.db.impl.connection.ConnectionFactory
import trw.dbsubsetter.db.{Row, SchemaInfo, Sql, Table, TargetDbAccess}

private[db] class TargetDbAccessImpl(connStr: String, sch: SchemaInfo, connectionFactory: ConnectionFactory) extends TargetDbAccess {

  private[this] val connection: Connection =
    connectionFactory.getConnectionWithWritePrivileges(connStr)

  private[this] val statements = Sql.preparedInsertStatementStrings(sch).map { case (table, sqlStr) =>
    table -> connection.prepareStatement(sqlStr)
  }

  // TODO reconsider Int return type if we just return 1 -- it's confusing
  override def insertRows(table: Table, rows: Vector[Row]): Int = {

    val stmt = statements(table)
    val cols = sch.colsByTableOrdered(table).size

    rows.foreach { row =>
      (1 to cols).foreach(i => stmt.setObject(i, row(i - 1)))
      stmt.addBatch()
    }

    stmt.executeBatch()
    1
  }

}