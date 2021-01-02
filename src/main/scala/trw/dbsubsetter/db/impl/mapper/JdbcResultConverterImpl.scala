package trw.dbsubsetter.db.impl.mapper

import trw.dbsubsetter.db.value.ColumnValue
import trw.dbsubsetter.db.{Column, ForeignKey, Keys, PrimaryKey, Row, SchemaInfo, Table}

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
    val pk: PrimaryKey = schemaInfo.pksByTable(table)
    val fk1: Seq[ForeignKey] = schemaInfo.
    val multipleRowsRawData = extractMultiRowRawData(jdbcResultSet, cols)
    multipleRowsRawData.map(singleRowRawData => new Keys(singleRowRawData)).toVector
  }

  private[this] def extractMultiRowRawData(jdbcResultSet: ResultSet, cols: Seq[Column]): Seq[Map[Column, Option[ColumnValue]]] = {
    val multipleRowsRawData = ArrayBuffer.empty[Map[Column, Any]]
    while (jdbcResultSet.next()) {
      if (jdbcResultSet.wasNull())
      val singleRowData: Map[Column, Any] = cols.map(col => col -> jdbcResultSet.getObject(col.name)).toMap
      multipleRowsRawData.append(singleRowData)
    }
    multipleRowsRawData
  }
}
