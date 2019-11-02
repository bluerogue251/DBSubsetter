package trw.dbsubsetter.pkvalueextraction

import trw.dbsubsetter.db.{KeyData, PrimaryKeyValue, SchemaInfo, Table}

object PkValueExtractionUtil {

  def pkValueExtractionFunctionsByTable(schemaInfo: SchemaInfo): Map[Table, KeyData => PrimaryKeyValue] = {
    schemaInfo.pksByTableOrdered.map { case (table, primaryKeyColumns) =>
      val primaryKeyExtractionFunction: KeyData => PrimaryKeyValue = keyData => {
        val individualColumnValues: Seq[Any] = primaryKeyColumns.map(keyData.get)
        new PrimaryKeyValue(individualColumnValues)
      }
      table -> primaryKeyExtractionFunction
    }
  }

}
