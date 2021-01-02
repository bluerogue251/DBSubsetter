package trw.dbsubsetter.db.impl.mapper

import trw.dbsubsetter.db.value.ColumnValue
import trw.dbsubsetter.db.{Column, ForeignKey, Keys, PrimaryKey, Row, SchemaInfo, Table}

import java.sql.ResultSet
import scala.collection.mutable.ArrayBuffer

private[db] class JdbcResultConverterImpl(schemaInfo: SchemaInfo) extends JdbcResultConverter {

  def convertToRows(jdbcResultSet: ResultSet, table: Table): Vector[Row] = {
    val cols: Seq[Column] = schemaInfo.dataColumnsByTable(table)
    val multipleRowsRawData = extractRows(jdbcResultSet, cols)
    multipleRowsRawData.map(singleRowRawData => new Row(singleRowRawData)).toVector
  }

  override def convertToKeys(jdbcResultSet: ResultSet, table: Table): Vector[Keys] = {
    val cols: Seq[Column] = schemaInfo.keyColumnsByTable(table)
    val pk: PrimaryKey = schemaInfo.pksByTable(table)
    val fksFromTable: Seq[ForeignKey] = schemaInfo.fksFromTable(table)
    val fksToTable: Seq[ForeignKey] = schemaInfo.fksToTable(table)
    val rows: Seq[Map[Column, Option[ColumnValue]]] = extractRows(jdbcResultSet, cols)
    rows.map { row =>
      pk.columns.map(row)
    }
  }

  private[this] def extractRows(jdbcResultSet: ResultSet, cols: Seq[Column]): Seq[Map[Column, Option[ColumnValue]]] = {
    val multipleRowsRawData = ArrayBuffer.empty[Map[Column, Any]]
    while (jdbcResultSet.next()) {
      val singleRowData: Map[Column, Any] = cols.map(col => col -> jdbcResultSet.getObject(col.name)).toMap
      multipleRowsRawData.append(singleRowData)
    }
    multipleRowsRawData
  }

  private[this] def extractRow(jdbcResultSet: ResultSet, cols: Seq[Column]): Map[Column, Option[ColumnValue]] = {
    cols.map(col => col -> col.getValueMaybe(jdbcResultSet)).toMap
  }
}
