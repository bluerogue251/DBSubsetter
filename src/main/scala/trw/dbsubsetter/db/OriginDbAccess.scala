package trw.dbsubsetter.db

import java.sql.{DriverManager, ResultSet}

import scala.collection.mutable.ArrayBuffer

class OriginDbAccess(connStr: String, sch: SchemaInfo) {
  private val originConn = DriverManager.getConnection(connStr)
  originConn.setReadOnly(true)
  private val statements = Sql.preparedQueryStatementStrings(sch).map { case ((fk, table), sqlStr) =>
    (fk, table) -> originConn.prepareStatement(sqlStr)
  }

  def getRowsFromTemplate(fk: ForeignKey, table: Table, fkValue: AnyRef): Vector[Row] = {
    val stmt = statements(fk, table)
    stmt.clearParameters()
    if (fk.isSingleCol) {
      stmt.setObject(1, fkValue)
    } else {
      fkValue.asInstanceOf[Vector[AnyRef]].zipWithIndex.foreach { case (value, i) =>
        stmt.setObject(i + 1, value)
      }
    }
    val jdbcResult = stmt.executeQuery()
    jdbcResultToRows(jdbcResult, table)
  }

  def getRows(query: SqlQuery, table: Table): Vector[Row] = {
    val jdbcResult = originConn.createStatement().executeQuery(query)
    jdbcResultToRows(jdbcResult, table)
  }

  private def jdbcResultToRows(res: ResultSet, table: Table): Vector[Row] = {
    val cols = sch.colsByTable(table).size
    val rows = ArrayBuffer.empty[Row]
    while (res.next()) {
      val row = new Array[AnyRef](cols)
      (1 to cols).foreach(i => row(i - 1) = res.getObject(i))
      rows += row
    }
    rows.toVector
  }
}