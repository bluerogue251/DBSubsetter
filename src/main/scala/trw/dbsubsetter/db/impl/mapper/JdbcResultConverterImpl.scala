package trw.dbsubsetter.db.impl.mapper

import trw.dbsubsetter.db.{Column, Keys, Row, SchemaInfo, Table}

import java.sql.ResultSet
import scala.collection.mutable.ArrayBuffer

private[db] class JdbcResultConverterImpl(schemaInfo: SchemaInfo) extends JdbcResultConverter {

  def convertToRows(jdbcResultSet: ResultSet, table: Table): Vector[Row] = {
    val cols: Seq[Column] = schemaInfo.dataColumnsByTable(table)
    val multipleRowsRawData = extractMultiRowRawData(jdbcResultSet, cols)
    multipleRowsRawData.map(singleRowRawData => new Row(singleRowRawData)).toVector
  }

  override def convertToKeys(jdbcResultSet: ResultSet, table: Table): Vector[Keys] = {
    val cols: Seq[Column] = schemaInfo.keyColumnsByTable(table)
    val multipleRowsRawData = extractMultiRowRawData(jdbcResultSet, cols)
    multipleRowsRawData.map(singleRowRawData => new Keys(singleRowRawData)).toVector
  }

  private[this] def extractMultiRowRawData(jdbcResultSet: ResultSet, cols: Seq[Column]): Seq[Map[Column, Any]] = {
    val multipleRowsRawData = ArrayBuffer.empty[Map[Column, Any]]
    while (jdbcResultSet.next()) {
      val singleRowData: Map[Column, Any] = cols.map(col => col -> jdbcResultSet.getObject(col.name)).toMap
      multipleRowsRawData.append(singleRowData)
    }
    multipleRowsRawData
  }
}
