package trw.dbsubsetter.db.impl.mapper

import java.sql.ResultSet

import trw.dbsubsetter.db.{Keys, Row, SchemaInfo, Table}

import scala.collection.mutable.ArrayBuffer

private[db] class JdbcResultConverterImpl(schemaInfo: SchemaInfo) extends JdbcResultConverter {

  def convertToRows(jdbcResultSet: ResultSet, table: Table): Vector[Row] = {
    val cols = schemaInfo.dataColumnsByTableOrdered(table).size
    val rows = ArrayBuffer.empty[Row]
    while (jdbcResultSet.next()) {
      val row = new Row(cols)
      (1 to cols).foreach(i => row(i - 1) = jdbcResultSet.getObject(i))
      rows += row
    }
    rows.toVector
  }

  override def convertToKeys(jdbcResultSet: ResultSet, table: Table): Vector[Keys] = {
    convertToRows(jdbcResultSet, table).map(data => new Keys(data))
  }
}
