package trw.dbsubsetter.db

import trw.dbsubsetter.config.{ConfigColumn, SchemaConfig}

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

    /*
     * Detect missing tables from --primaryKey
     */
    schemaConfig.extraPrimaryKeys.foreach { extraPrimaryKey =>
      if (!actualTables.contains(extraPrimaryKey.table)) {
        return missingTable("--primaryKey", extraPrimaryKey.table)
      }
    }

    /*
     * Detect missing tables from --foreignKey
     */
    schemaConfig.extraForeignKeys.foreach { extraForeignKey =>
      if (!actualTables.contains(extraForeignKey.fromTable)) {
        return missingTable("--foreignKey", extraForeignKey.fromTable)
      }
      if (!actualTables.contains(extraForeignKey.toTable)) {
        return missingTable("--foreignKey", extraForeignKey.toTable)
      }
    }

    /*
     * Detect missing tables from --excludeTable
     */
    schemaConfig.excludeTables.foreach { excludeTable =>
      if (!actualTables.contains(excludeTable)) {
        return missingTable("--excludeTable", excludeTable)
      }
    }

    /*
     * Detect missing tables from --excludeColumns
     */
    schemaConfig.excludeColumns.foreach { excludeColumn =>
      if (!actualTables.contains(excludeColumn.table)) {
        return missingTable("--excludeColumns", excludeColumn.table)
      }
    }

    val actualColumns: Set[ConfigColumn] =
      dbMeta.columns
        .map(columnQueryRow => {
          val schema = Schema(columnQueryRow.schema)
          val table = Table(schema, columnQueryRow.table)
          ConfigColumn(table, columnQueryRow.name)
        })
        .toSet

    /*
     * Detect missing columns from --primaryKey
     */
    schemaConfig.extraPrimaryKeys
      .flatMap(_.columns)
      .foreach { primaryKeyColumn =>
        if (!actualColumns.contains(primaryKeyColumn)) {
          return missingColumn("--primaryKey", primaryKeyColumn)
        }
      }

    /*
     * Detect missing columns from --foreignKey
     */
    schemaConfig.extraForeignKeys
      .flatMap(fk => fk.fromColumns ++ fk.toColumns)
      .foreach { foreignKeyColumn =>
        if (!actualColumns.contains(foreignKeyColumn)) {
          return missingColumn("--foreignKey", foreignKeyColumn)
        }
      }

    /*
     * Detect missing columns from --excludeColumns
     */
    schemaConfig.excludeColumns
      .foreach { excludeColumn =>
        if (!actualColumns.contains(excludeColumn)) {
          return missingColumn("--excludeColumns", excludeColumn)
        }
      }

    OK
  }

  private def missingTable(option: String, table: Table): ValidationError = {
    ValidationError(s"Table ${display(table)} specified in $option not found in database")
  }

  private def missingColumn(option: String, column: ConfigColumn): ValidationError = {
    ValidationError(s"Column ${display(column)} specified in $option not found in database")
  }

  private def display(table: Table): String = {
    s"'${table.schema.name}.${table.name}'"
  }

  private def display(column: ConfigColumn): String = {
    s"'${column.table.schema.name}.${column.table.name}.${column.name}'"
  }
}

sealed trait SchemaValidationResult
case object OK extends SchemaValidationResult
case class ValidationError(message: String) extends SchemaValidationResult
