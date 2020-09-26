package trw.dbsubsetter.keyextraction

import trw.dbsubsetter.db.{ForeignKey, ForeignKeyValue, Keys, PrimaryKeyValue, SchemaInfo, Table}

object KeyExtractionUtil {

  def pkExtractionFunctions(schemaInfo: SchemaInfo): Map[Table, Keys => PrimaryKeyValue] = {
    schemaInfo.pksByTable.map { case (table, primaryKey) =>
      val primaryKeyExtractionFunction: Keys => PrimaryKeyValue = keys => keys.getValue(primaryKey)
      table -> primaryKeyExtractionFunction
    }
  }

  def fkExtractionFunctions(schemaInfo: SchemaInfo): Map[(ForeignKey, Boolean), Keys => ForeignKeyValue] = {
    schemaInfo.fksOrdered.flatMap { foreignKey =>
      val parentExtractionFunction: Keys => ForeignKeyValue =
        keys => keys.getValue(foreignKey, confusingTechDebt = false)

      val childExtractionFunction: Keys => ForeignKeyValue =
        keys => keys.getValue(foreignKey, confusingTechDebt = true)

      Seq(
        (foreignKey, false) -> parentExtractionFunction,
        (foreignKey, true) -> childExtractionFunction
      )
    }.toMap
  }
}
