package trw.dbsubsetter.keyextraction

import trw.dbsubsetter.db.{ForeignKey, ForeignKeyValue, Keys, PrimaryKeyValue, SchemaInfo, Table}

object KeyExtractionUtil {

  def pkExtractionFunctions(schemaInfo: SchemaInfo): Map[Table, Keys => PrimaryKeyValue] = {
    schemaInfo.pksByTable.map { case (table, primaryKey) =>
      val primaryKeyColumnOrdinals: Seq[Int] = primaryKey.columns.map(_.ordinalPosition)
      val primaryKeyExtractionFunction: Keys => PrimaryKeyValue = keys => {
        val individualColumnValues: Seq[Any] = primaryKeyColumnOrdinals.map(keys.data)
        new PrimaryKeyValue(individualColumnValues)
      }
      table -> primaryKeyExtractionFunction
    }
  }

  def fkExtractionFunctions(schemaInfo: SchemaInfo): Map[(ForeignKey, Boolean), Keys => ForeignKeyValue] = {
    schemaInfo.fksOrdered.flatMap { foreignKey =>

      val parentExtractionOrdinalPositions =
        foreignKey.fromCols.map(_.ordinalPosition)
      val parentExtractionFunction: Keys => ForeignKeyValue =
        keys => new ForeignKeyValue(parentExtractionOrdinalPositions.map(keys.data))

      val childExtractionOrdinalPositions =
        foreignKey.toCols.map(_.ordinalPosition)
      val childExtractionFunction: Keys => ForeignKeyValue =
        keys => new ForeignKeyValue(childExtractionOrdinalPositions.map(keys.data))

      Seq(
        (foreignKey, false) -> parentExtractionFunction,
        (foreignKey, true) -> childExtractionFunction
      )
    }.toMap
  }
}
