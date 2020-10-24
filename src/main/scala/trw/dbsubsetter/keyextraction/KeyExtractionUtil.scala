package trw.dbsubsetter.keyextraction

import trw.dbsubsetter.db.{Keys, MultiColumnPrimaryKeyValue, SchemaInfo, Table}

object KeyExtractionUtil {

  def pkExtractionFunctions(schemaInfo: SchemaInfo): Map[Table, Keys => MultiColumnPrimaryKeyValue] = {
    schemaInfo.pksByTable.map { case (table, primaryKey) =>
      val primaryKeyExtractionFunction: Keys => MultiColumnPrimaryKeyValue = keys => keys.getValue(primaryKey)
      table -> primaryKeyExtractionFunction
    }
  }

}
