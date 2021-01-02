package trw.dbsubsetter.db.impl.mapper

import trw.dbsubsetter.db.{Column, Keys, Row, SchemaInfo, Table}

import java.sql.ResultSet
import scala.collection.mutable.ArrayBuffer

private[db] class JdbcResultConverterImpl(schemaInfo: SchemaInfo) extends JdbcResultConverter {

  def convertToRows(jdbcResultSet: ResultSet, table: Table): Vector[Row] = {
    val cols: Seq[Column] = schemaInfo.dataColumnsByTable(table)
    val multipleRowsRawData = extractMultiRowRawData(jdbcResultSet, cols.size)
    multipleRowsRawData.map(singleRowRawData => new Row(singleRowRawData)).toVector
  }

  override def convertToKeys(jdbcResultSet: ResultSet, table: Table): Seq[Keys] = {
    val cols: Seq[Column] = schemaInfo.keyColumnsByTable(table)
    val multipleRowsRawData = extractMultiRowRawData(jdbcResultSet, cols.size)
    multipleRowsRawData.map(singleRowRawData => new Keys(singleRowRawData)).toVector
  }

  private[this] def extractMultiRowRawData(jdbcResultSet: ResultSet, columnCount: Int): Seq[Array[Any]] = {
    val multipleRowsRawData = ArrayBuffer.empty[Array[Any]]
    while (jdbcResultSet.next()) {
      val singleRowRawData = new Array[Any](columnCount)
      (1 to columnCount).foreach { i =>
        singleRowRawData(i - 1) = jdbcResultSet.getObject(i)
      }
      multipleRowsRawData += singleRowRawData
    }
    multipleRowsRawData
  }
}
