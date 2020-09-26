package trw.dbsubsetter.db.impl.target

import java.sql.Connection

import trw.dbsubsetter.db.{Row, SchemaInfo, Sql, Table, TargetDbAccess}
import trw.dbsubsetter.db.impl.ConnectionFactory

private[db] class TargetDbAccessImpl(connStr: String, sch: SchemaInfo, connectionFactory: ConnectionFactory)
    extends TargetDbAccess {

  private[this] val connection: Connection =
    connectionFactory.getReadWriteConnection(connStr)

  private[this] val statements = Sql.insertSqlTemplates(sch).map { case (table, sqlQuery) =>
    table -> connection.prepareStatement(sqlQuery.value)
  }

  override def insertRows(table: Table, rows: Vector[Row]): Unit = {
    val stmt = statements(table)

    rows.foreach { row =>
      row
        .data
        .zipWithIndex
        .foreach { case (singleColumnValue, i) =>
          stmt.setObject(i + 1, singleColumnValue)
        }

      stmt.addBatch()
    }

    stmt.executeBatch()
  }
}
