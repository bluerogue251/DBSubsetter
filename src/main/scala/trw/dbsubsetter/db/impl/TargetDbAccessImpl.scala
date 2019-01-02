package trw.dbsubsetter.db.impl

import java.sql.Connection

import trw.dbsubsetter.db.{Row, SchemaInfo, Sql, Table, TargetDbAccess}

private[db] class TargetDbAccessImpl(connStr: String, sch: SchemaInfo, connectionFactory: ConnectionFactory) extends TargetDbAccess {

  private val conn: Connection = connectionFactory.getConnectionWithWritePrivileges(connStr)

  private val statements = Sql.preparedInsertStatementStrings(sch).map { case (table, sqlStr) =>
    table -> conn.prepareStatement(sqlStr)
  }

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