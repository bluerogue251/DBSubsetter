package trw.dbsubsetter.db

import java.sql.DriverManager

class TargetDbAccess(connStr: String, sch: SchemaInfo) {
  private val conn = DriverManager.getConnection(connStr)
  if (conn.isMysql) {
    conn.createStatement().execute("SET SESSION SQL_MODE = ANSI_QUOTES")
    conn.createStatement().execute("SET SESSION FOREIGN_KEY_CHECKS = 0")
  }
  private val statements = Sql.preparedInsertStatementStrings(sch).map { case (table, sqlStr) =>
    table -> conn.prepareStatement(sqlStr)
  }

  def insertRows(table: Table, rows: Vector[Row]): Int = {
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