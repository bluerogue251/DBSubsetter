package trw.dbsubsetter.fkcalc

import trw.dbsubsetter.db.{ForeignKey, ForeignKeyValue, Keys, SchemaInfo}

private[fkcalc] object FkExtractor {
  def fkExtractionFunctions(schemaInfo: SchemaInfo): Map[(ForeignKey, Boolean), Keys => ForeignKeyValue] = {
    schemaInfo.foreignKeys.flatMap { foreignKey =>
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
