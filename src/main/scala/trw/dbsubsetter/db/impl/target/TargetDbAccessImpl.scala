package trw.dbsubsetter.db.impl.target

import java.sql.Connection

import trw.dbsubsetter.db.impl.connection.ConnectionFactory
import trw.dbsubsetter.db.{Row, SchemaInfo, Sql, Table, TargetDbAccess}

private[db] class TargetDbAccessImpl(connStr: String, sch: SchemaInfo, connectionFactory: ConnectionFactory) extends TargetDbAccess {

  private[this] val connection: Connection =
    connectionFactory.getConnectionWithWritePrivileges(connStr)

  private[this] val statements = Sql.insertSqlTemplates(sch).map { case (table, sqlStr) =>
    table -> connection.prepareStatement(sqlStr)
  }

  override def insertRows(table: Table, rows: Vector[Row]): Unit = {

    val stmt = statements(table)
    val cols = sch.dataColumnsByTableOrdered(table).size

    rows.foreach { row =>
      (1 to cols).foreach(i => stmt.setObject(i, row(i - 1)))
      stmt.addBatch()
    }

    stmt.executeBatch()
  }
}
