package trw.dbsubsetter.db.impl.target

import trw.dbsubsetter.db
import trw.dbsubsetter.db.impl.ConnectionFactory
import trw.dbsubsetter.db.{Row, SchemaInfo, Sql, Table, TargetDbAccess}

import java.sql.{Connection, PreparedStatement}

private[db] class TargetDbAccessImpl(connStr: String, sch: SchemaInfo, connectionFactory: ConnectionFactory)
    extends TargetDbAccess {

  private[this] val connection: Connection =
    connectionFactory.getReadWriteConnection(connStr)

  private[this] val statements: Map[Table, PreparedStatement] =
    Sql.insertSqlTemplates(sch).map { case (table, sqlQuery) =>
      table -> connection.prepareStatement(sqlQuery.value)
    }

  private[this] val colsWithIndex: Map[Table, Seq[(db.Column, Int)]] =
    sch.dataColumnsByTable
      .map { case (table, columns) =>
        table -> columns.zipWithIndex
      }

  override def insertRows(table: Table, rows: Vector[Row]): Unit = {
    val stmt = statements(table)
    val cols = colsWithIndex(table)

    rows.foreach { row =>
      cols.foreach { case (col, i) =>
        stmt.setObject(i + 1, row.data(col))
      }
      stmt.addBatch()
    }

    stmt.executeBatch()
  }
}
