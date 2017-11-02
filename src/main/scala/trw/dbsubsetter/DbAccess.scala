package trw.dbsubsetter

import java.sql.Connection

import scala.collection.mutable.ArrayBuffer

object DbAccess {
  def getRows(conn: Connection, query: SqlQuery, columnsToSelect: Seq[Column]): Seq[Row] = {
    // Put the result in a collection of Maps from column names to values, each element in the collection is a row of the result
    // Could we be more efficient by doing this by index rather than by column name?
    val jdbcResult = conn.createStatement().executeQuery(query)
    val rows = ArrayBuffer.empty[Row]
    while (jdbcResult.next()) {
      rows += columnsToSelect.map(col => col.name -> jdbcResult.getObject(col.name)).toMap
    }
    rows
  }
}