package trw.dbsubsetter.keyextraction

import trw.dbsubsetter.db.{Keys, PrimaryKeyValue, SchemaInfo, Table}

object KeyExtractionUtil {

  def pkExtractionFunctions(schemaInfo: SchemaInfo): Map[Table, Keys => PrimaryKeyValue] = {
    schemaInfo.pksByTable.map { case (table, primaryKey) =>
      val primaryKeyExtractionFunction: Keys => PrimaryKeyValue = keys => keys.getValue(primaryKey)
      table -> primaryKeyExtractionFunction
    }
  }

}
