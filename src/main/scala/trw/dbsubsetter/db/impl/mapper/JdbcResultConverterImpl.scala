package trw.dbsubsetter.db.impl.mapper

import trw.dbsubsetter.db.value.ColumnValue
import trw.dbsubsetter.db.{Column, ForeignKey, Keys, PrimaryKey, Row, SchemaInfo, Table}

import java.sql.ResultSet
import scala.collection.mutable.ArrayBuffer

private[db] class JdbcResultConverterImpl(schemaInfo: SchemaInfo) extends JdbcResultConverter {

  def convertToRows(jdbcResultSet: ResultSet, table: Table): Vector[Row] = {
    val cols: Seq[Column] = schemaInfo.dataColumnsByTable(table)
    val multipleRowsRawData = ArrayBuffer.empty[Row]
    while (jdbcResultSet.next()) {
      val m: Map[Column, Any] = cols.map { col => col -> jdbcResultSet.getObject(col.name) }.toMap
      multipleRowsRawData.append(new Row(m))
    }
    multipleRowsRawData.toVector
  }

  override def convertToKeys(jdbcResultSet: ResultSet, table: Table): Vector[Keys] = {
    val cols: Seq[Column] = schemaInfo.keyColumnsByTable(table)
    val pk: PrimaryKey = schemaInfo.pksByTable(table)
    val fksFromTable: Seq[ForeignKey] = schemaInfo.fksFromTable(table)
    val fksToTable: Seq[ForeignKey] = schemaInfo.fksToTable(table)
    val rows: Seq[Map[Column, Option[ColumnValue]]] = extractMaps(jdbcResultSet, cols)
    rows.map { row =>
      pk.columns.map(row)
    }
    ???
  }
}
