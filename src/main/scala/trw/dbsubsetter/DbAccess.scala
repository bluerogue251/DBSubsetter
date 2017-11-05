package trw.dbsubsetter

import java.sql.{Connection, PreparedStatement, ResultSet}

import scala.collection.mutable.ArrayBuffer

// Put the result in a collection of Maps from column names to values, each element in the collection is a row of the result
// Could we be more efficient by doing this by index rather than by column name?
object DbAccess {
  def getRows(preparedStatment: PreparedStatement, params: Seq[AnyRef], cols: Seq[Column]): Seq[Row] = {
    params.zipWithIndex.foreach { case (value, i) =>
      preparedStatment.setObject(i + 1, value)
    }
    val jdbcResult = preparedStatment.executeQuery()
    preparedStatment.clearParameters()
    jdbcResultToRows(jdbcResult, cols)
  }

  def getRows(conn: Connection, query: SqlQuery, cols: Seq[Column]): Seq[Row] = {
    val jdbcResult = conn.createStatement().executeQuery(query)
    jdbcResultToRows(jdbcResult, cols)
  }

  private def jdbcResultToRows(res: ResultSet, cols: Seq[Column]): Seq[Row] = {
    // Could we avoid using ArrayBuffer by knowing up front how many rows were fetched from DB?
    val rows = ArrayBuffer.empty[Row]
    while (res.next()) {
      rows += cols.map(col => col -> res.getObject(col.name)).toMap
    }
    rows
  }
}