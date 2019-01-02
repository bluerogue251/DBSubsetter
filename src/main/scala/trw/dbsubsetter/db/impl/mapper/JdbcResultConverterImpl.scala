package trw.dbsubsetter.db.impl.mapper

import java.sql.ResultSet

import trw.dbsubsetter.db.{Row, SchemaInfo, Table}

import scala.collection.mutable.ArrayBuffer

private[db] class JdbcResultConverterImpl(schemaInfo: SchemaInfo) extends JdbcResultConverter {

  def convert(res: ResultSet, table: Table): Vector[Row] = {
    val cols = schemaInfo.colsByTableOrdered(table).size
    val rows = ArrayBuffer.empty[Row]
    while (res.next()) {
      val row = new Row(cols)
      (1 to cols).foreach(i => row(i - 1) = res.getObject(i))
      rows += row
    }
    rows.toVector
  }

}
