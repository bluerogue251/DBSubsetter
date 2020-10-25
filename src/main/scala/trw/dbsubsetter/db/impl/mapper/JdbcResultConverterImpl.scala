package trw.dbsubsetter.db.impl.mapper

import java.sql.ResultSet

import trw.dbsubsetter.db.{Column, Keys, Row, SchemaInfo, Table}

import scala.collection.mutable.ArrayBuffer

private[db] class JdbcResultConverterImpl(schemaInfo: SchemaInfo) extends JdbcResultConverter {

  def convertToRows(jdbcResultSet: ResultSet, table: Table): Vector[Row] = {
    val cols: Seq[Column] = schemaInfo.dataColumnsByTableOrdered(table)
    val multipleRowsRawData = extractMultiRowRawData(jdbcResultSet, cols.size)
    multipleRowsRawData.map(singleRowRawData => new Row(singleRowRawData)).toVector
  }

  override def convertToKeys(jdbcResultSet: ResultSet, table: Table): Vector[Keys] = {
    val cols: Seq[Column] = schemaInfo.keyColumnsByTableOrdered(table)
    val multipleRowsRawData = extractMultiRowRawData(jdbcResultSet, cols.size)
    multipleRowsRawData.map(singleRowRawData => new Keys(singleRowRawData)).toVector
  }

  private[this] def extractMultiRowRawData(jdbcResultSet: ResultSet, columnCount: Int): Seq[Array[Any]] = {
    val multipleRowsRawData = ArrayBuffer.empty[Array[Any]]
    while (jdbcResultSet.next()) {
      val singleRowRawData = new Array[Any](columnCount)
      (1 to columnCount).foreach { i =>
        singleRowRawData(i - 1) = jdbcResultSet.getBytes(i)
      }
      multipleRowsRawData += singleRowRawData
    }
    multipleRowsRawData
  }
}
