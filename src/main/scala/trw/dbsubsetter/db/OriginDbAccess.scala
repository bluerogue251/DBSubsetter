package trw.dbsubsetter.db

import java.sql.{DriverManager, ResultSet}

import scala.collection.mutable.ArrayBuffer

// Put the result in a collection of Maps from column names to values, each element in the collection is a row of the result
// Could we be more efficient by doing this by index rather than by column name?
class OriginDbAccess(connStr: String, sch: SchemaInfo) {
  private val originConn = DriverManager.getConnection(connStr)
  originConn.setReadOnly(true)
  private val statements = Sql.preparedQueryStatementStrings(sch).map { case ((fk, table), sqlStr) =>
    (fk, table) -> originConn.prepareStatement(sqlStr)
  }

  def getRowsFromTemplate(fk: ForeignKey, table: Table, params: Seq[AnyRef]): Vector[Row] = {
    val stmt = statements(fk, table)
    stmt.clearParameters()
    params.zipWithIndex.foreach { case (value, i) =>
      stmt.setObject(i + 1, value)
    }
    val jdbcResult = stmt.executeQuery()
    jdbcResultToRows(jdbcResult, table)
  }

  def getRows(query: SqlQuery, table: Table): Vector[Row] = {
    val jdbcResult = originConn.createStatement().executeQuery(query)
    jdbcResultToRows(jdbcResult, table)
  }

  private def jdbcResultToRows(res: ResultSet, table: Table): Vector[Row] = {
    // Could we avoid using ArrayBuffer by knowing up front how many rows were fetched from DB?
    val cols = sch.colsByTable(table)

    val rows = ArrayBuffer.empty[Row]

    while (res.next()) {
      rows += cols.map(col => col -> res.getObject(col)).toMap
    }
    rows.toVector
  }
}