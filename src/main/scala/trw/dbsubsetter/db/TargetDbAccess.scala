package trw.dbsubsetter.db

import java.sql.DriverManager

class TargetDbAccess(connStr: String, sch: SchemaInfo) {
  private val connection = DriverManager.getConnection(connStr)
  private val statements = Sql.preparedInsertStatementStrings(sch).map { case (table, sqlStr) =>
    table -> connection.prepareStatement(sqlStr)
  }

  def insertRows(table: Table, rows: Vector[Row]): Int = {
    val stmt = statements(table)
    val cols = sch.colsByTable(table).size

    rows.foreach { row =>
      (1 to cols).foreach(i => stmt.setObject(i, row(i - 1)))
      stmt.addBatch()
    }

    stmt.executeBatch()
    1
  }
}