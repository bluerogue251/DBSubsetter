package trw.dbsubsetter.db

import java.sql.DriverManager

class TargetDbAccess(connStr: String, sch: SchemaInfo) {
  private val connection = DriverManager.getConnection(connStr)
  private val statements = Sql.preparedInsertStatementStrings(sch).map { case (table, sqlStr) =>
    table -> connection.prepareStatement(sqlStr)
  }

  def insertRows(table: Table, rows: Vector[Row]): Int = {
    val stmt = statements(table)
    val cols = sch.colsByTable(table)

    rows.foreach { row =>
      cols.zipWithIndex.foreach { case (colName, i) =>
        stmt.setObject(i + 1, row(colName))
      }
      stmt.addBatch()
    }

    stmt.executeBatch()
    1
  }
}