package trw.dbsubsetter.config

import trw.dbsubsetter.db.{Schema, Table}

/**
  * Extract command line supplied config values which are simple strings into
  * richer types. If invalid input prevents this, surface this as a validation error.
  */
class ConfigExtractor {
  def extractSchemaConfig(input: CommandLineConfig): SchemaConfigExtractionResult = {
    val schemas = input.schemas.map(Schema)

    val baseQueryRegex = """^\s*(.+)\.(.+)\s+:::\s+(.+)\s+:::\s+(includeChildren|excludeChildren)\s*$""".r
    val baseQueries =
      input.baseQueries
        .map {
          case baseQueryRegex(schemaName, tableName, whereClause, includeChildren) =>
            val table = normalizeTable(schemaName, tableName)
            ConfigBaseQuery(table, whereClause.trim, includeChildren == "includeChildren")
          case baseQueryString =>
            return InvalidInput(InvalidBaseQuery(baseQueryString))
        }

    val foreignKeyRegex = """^(.+)\.(.+)\((.+)\)\s+:::\s+(.+)\.(.+)\((.+)\)\s*$""".r
    val extraForeignKeys =
      input.extraForeignKeys
        .map {
          case foreignKeyRegex(fromSchemaName, fromTableName, fromCols, toSchemaName, toTableName, toCols) =>
            val fromTable = normalizeTable(fromSchemaName, fromTableName)
            val toTable = normalizeTable(toSchemaName, toTableName)
            ConfigForeignKey(
              fromTable = fromTable,
              fromColumns = normalizeColumns(fromTable, fromCols),
              toTable = toTable,
              toColumns = normalizeColumns(toTable, toCols)
            )
          case foreignKeyString =>
            return InvalidInput(InvalidExtraForeignKey(foreignKeyString))
        }

    val primaryKeyRegex = """^\s*(.+)\.(.+)\((.+)\)\s*$""".r
    val extraPrimaryKeys =
      input.extraPrimaryKeys
        .map {
          case primaryKeyRegex(schemaName, tableName, cols) =>
            val table = normalizeTable(schemaName, tableName)
            val columns = normalizeColumns(table, cols)
            ConfigPrimaryKey(table, columns)
          case primaryKeyString =>
            return InvalidInput(InvalidExtraPrimaryKey(primaryKeyString))
        }

    Valid(
      SchemaConfig(
        schemas = schemas,
        baseQueries = baseQueries,
        extraForeignKeys = extraForeignKeys,
        extraPrimaryKeys = extraPrimaryKeys,
        excludeTables = ???,
        excludeColumns = ???
      )
    )
  }

  private[this] def normalizeTable(schemaName: String, tableName: String): Table = {
    val schema = Schema(schemaName.trim)
    Table(schema = schema, name = tableName.trim)
  }

  private[this] def normalizeColumns(table: Table, untrimmedColumnCsvs: String): Seq[ConfigColumn] = {
    untrimmedColumnCsvs
      .split(",")
      .map(_.trim)
      .map(columnName => ConfigColumn(table, columnName))
  }
}

sealed trait SchemaConfigExtractionResult
case class Valid(schemaConfig: SchemaConfig) extends SchemaConfigExtractionResult
case class InvalidInput(errorType: ErrorType) extends SchemaConfigExtractionResult

sealed trait ErrorType
case class InvalidBaseQuery(input: String) extends ErrorType
case class InvalidExtraPrimaryKey(input: String) extends ErrorType
case class InvalidExtraForeignKey(input: String) extends ErrorType
