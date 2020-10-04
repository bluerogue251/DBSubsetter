package trw.dbsubsetter.db

import trw.dbsubsetter.config.SchemaConfig

object SchemaValidation {

  def validate(schemaConfig: SchemaConfig, dbMeta: DbMetadataQueryResult): SchemaValidationResult = {
    val actualSchemas: Set[Schema] =
      dbMeta.schemas
        .map(schemaQueryRow => Schema(schemaQueryRow.name))
        .toSet

    /*
     * Detect Missing Schemas
     */
    val missingSchemas: Set[Schema] = schemaConfig.schemas.filterNot(actualSchemas)

    if (missingSchemas.nonEmpty) {
      val csv = missingSchemas.map(_.name).mkString(", ")
      return ValidationError(s"Specified --schemas not found in database: $csv")
    }

    val actualTables: Set[Table] =
      dbMeta.tables
        .map(tableQueryRow => Table(Schema(tableQueryRow.schema), tableQueryRow.name))
        .toSet

    /*
     * Detect missing tables from base queries
     */
    schemaConfig.baseQueries.foreach { baseQuery =>
      if (!actualTables.contains(baseQuery.table)) {
        return missingTable("--baseQuery", baseQuery.table)
      }
    }

    schemaConfig.extraPrimaryKeys.foreach { extraPrimaryKey =>
      if (!actualTables.contains(extraPrimaryKey.table)) {
        return missingTable("--primaryKey", extraPrimaryKey.table)
      }
    }

    schemaConfig.extraForeignKeys.foreach { extraForeignKey =>
      if (!actualTables.contains(extraForeignKey.fromTable)) {
        return missingTable("--foreignKey", extraForeignKey.fromTable)
      }
      if (!actualTables.contains(extraForeignKey.toTable)) {
        return missingTable("--foreignKey", extraForeignKey.toTable)
      }
    }

    schemaConfig.excludeTables.foreach { excludeTable =>
      if (!actualTables.contains(excludeTable)) {
        return missingTable("--excludeTable", excludeTable)
      }
    }

    schemaConfig.excludeColumns.foreach { excludeColumn =>
      if (!actualTables.contains(excludeColumn.table)) {
        return missingTable("--excludeColumns", excludeColumn.table)
      }
    }

    OK
  }

  private def missingTable(option: String, table: Table): ValidationError = {
    ValidationError(s"Table ${display(table)} specified in $option not found in database")
  }

  private def display(table: Table): String = {
    s"'${table.schema.name}.${table.name}'"
  }
}

sealed trait SchemaValidationResult
case object OK extends SchemaValidationResult
case class ValidationError(message: String) extends SchemaValidationResult
