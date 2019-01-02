package trw.dbsubsetter.db.impl

import java.sql.ResultSet

import trw.dbsubsetter.db.{ForeignKey, OriginDbAccess, Row, SchemaInfo, Sql, SqlQuery, Table}

import scala.collection.mutable.ArrayBuffer

private[db] class OriginDbAccessImpl(connStr: String, sch: SchemaInfo, connectionFactory: ConnectionFactory) extends OriginDbAccess {

  private val conn = connectionFactory.getReadOnlyConnection(connStr)

  private val statements = Sql.preparedQueryStatementStrings(sch).map { case ((fk, table), sqlStr) =>
    (fk, table) -> conn.prepareStatement(sqlStr)
  }

  def getRowsFromTemplate(fk: ForeignKey, table: Table, fkValue: Any): Vector[Row] = {
    val stmt = statements(fk, table)
    stmt.clearParameters()
    if (fk.isSingleCol) {
      stmt.setObject(1, fkValue)
    } else {
      fkValue.asInstanceOf[Array[Any]].zipWithIndex.foreach { case (value, i) =>
        stmt.setObject(i + 1, value)
      }
    }

    val jdbcResult = stmt.executeQuery()
    jdbcResultToRows(jdbcResult, table)
  }

  def getRows(query: SqlQuery, table: Table): Vector[Row] = {
    val jdbcResult = conn.createStatement().executeQuery(query)
    jdbcResultToRows(jdbcResult, table)
  }

  private def jdbcResultToRows(res: ResultSet, table: Table): Vector[Row] = {
    val cols = sch.colsByTableOrdered(table).size
    val rows = ArrayBuffer.empty[Row]
    while (res.next()) {
      val row = new Row(cols)
      (1 to cols).foreach(i => row(i - 1) = res.getObject(i))
      rows += row
    }
    rows.toVector
  }
}