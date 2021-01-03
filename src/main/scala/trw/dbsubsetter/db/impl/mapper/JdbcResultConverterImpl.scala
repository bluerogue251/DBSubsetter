package trw.dbsubsetter.db.impl.mapper

import trw.dbsubsetter.db.{Column, ForeignKey, Keys, PrimaryKey, Row, SchemaInfo, Table}

import java.sql.ResultSet
import scala.collection.mutable.ArrayBuffer

private[db] class JdbcResultConverterImpl(schemaInfo: SchemaInfo) extends JdbcResultConverter {

  def convertToRows(jdbcResultSet: ResultSet, table: Table): Vector[Row] = {
    val cols: Seq[Column] = schemaInfo.dataColumnsByTable(table)
    val multipleRowsRawData = ArrayBuffer.empty[Row]
    while (jdbcResultSet.next()) {
      val dataByColumn: Map[Column, Any] = cols.map { col => col -> jdbcResultSet.getObject(col.name) }.toMap
      multipleRowsRawData.append(new Row(dataByColumn))
    }
    multipleRowsRawData.toVector
  }

  override def convertToKeys(resultSet: ResultSet, table: Table): Vector[Keys] = {
    val pk: PrimaryKey = schemaInfo.pksByTable(table)
    val fks: Seq[ForeignKey] = schemaInfo.fksFromTable(table) ++ schemaInfo.fksFromTable(table)
    val multiRowKeys = ArrayBuffer.empty[Keys]
    while (resultSet.next()) {
      val pkValue = pk.extractValue(resultSet)
      val fkValues =
        fks.flatMap { fk =>
          fk.extractValue(table, resultSet).map { fkValue => fk -> fkValue }
        }.toMap
      multiRowKeys.append(new Keys(pkValue, fkValues))
    }
    multiRowKeys.toVector
  }
}
