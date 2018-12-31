package trw.dbsubsetter.db

class TargetDbAccess(connStr: String, sch: SchemaInfo, connectionFactory: ConnectionFactory) {

  private val conn = connectionFactory.getReadOnlyConnection()

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

  def close(): Unit = conn.close()
}