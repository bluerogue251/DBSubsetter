package trw.dbsubsetter.pkvalueextraction

import trw.dbsubsetter.db.{PrimaryKeyValue, Row, SchemaInfo, Table}

object PkValueExtractionUtil {

  def pkValueExtractionFunctionsByTable(schemaInfo: SchemaInfo): Map[Table, Row => PrimaryKeyValue] = {
    schemaInfo.pksByTableOrdered.map { case (table, primaryKeyColumns) =>
      val primaryKeyColumnOrdinals: Vector[Int] = primaryKeyColumns.map(_.ordinalPosition)
      val primaryKeyExtractionFunction: Row => PrimaryKeyValue = row => {
        val individualColumnValues: Seq[Any] = primaryKeyColumnOrdinals.map(row)
        new PrimaryKeyValue(individualColumnValues)
      }
      table -> primaryKeyExtractionFunction
    }
  }
}
