package trw.dbsubsetter.config

import trw.dbsubsetter.db.{Schema, Table}

import scala.util.matching.Regex

/**
  * Extract command line supplied config values which are simple strings into
  * richer types. If invalid input prevents this, surface this as a validation error.
  */
object ConfigExtractor {
  private val table: String = """([^.\s]*)\.([^.\s]+)"""
  private val columnSet: String = """([^.\s]+)\.([^.\s]+)\((.+)\)"""
  private val separator: String = """\s+:::\s+"""

  def extractConfig(args: CommandLineArgs): ExtractionResult = {
    val schemas = args.schemas.map(Schema)

    val baseQueryRegex = regex(table + separator + """(.+)""" + separator + """(includeChildren|excludeChildren)""")
    val baseQueries =
      args.baseQueries
        .map {
          case baseQueryRegex(schemaName, tableName, whereClause, includeChildren) =>
            val table = normalizeTable(schemaName, tableName)
            ConfigBaseQuery(table, whereClause.trim, includeChildren == "includeChildren")
          case baseQueryString =>
            return Invalid(InvalidBaseQuery(baseQueryString))
        }

    val primaryKeyRegex = regex(columnSet)
    val extraPrimaryKeys =
      args.extraPrimaryKeys
        .map {
          case primaryKeyRegex(schemaName, tableName, cols) =>
            val table = normalizeTable(schemaName, tableName)
            val columns = normalizeColumns(table, cols)
            ConfigPrimaryKey(table, columns)
          case primaryKeyString =>
            return Invalid(InvalidExtraPrimaryKey(primaryKeyString))
        }

    val duplicatePkTables: Set[Table] =
      extraPrimaryKeys.toSeq
        .map(_.table)
        .groupBy(identity)
        .filter { case (_, elems) => elems.size > 1 }
        .map { case (table, _) => table }
        .toSet
    if (duplicatePkTables.nonEmpty) {
      return Invalid(DuplicateExtraPrimaryKey(duplicatePkTables))
    }

    val foreignKeyRegex = regex(columnSet + separator + columnSet)
    val extraForeignKeys =
      args.extraForeignKeys
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
            return Invalid(InvalidExtraForeignKey(foreignKeyString))
        }

    val tableRegex = regex(table)
    val excludeTables =
      args.excludeTables
        .map {
          case tableRegex(schemaName, tableName) =>
            normalizeTable(schemaName, tableName)
          case excludeTableString =>
            return Invalid(InvalidExcludeTable(excludeTableString))
        }

    val columnRegex = regex(columnSet)
    val excludeColumns =
      args.excludeColumns
        .flatMap {
          case columnRegex(schemaName, tableName, cols) =>
            val table = normalizeTable(schemaName, tableName)
            normalizeColumns(table, cols).toSet
          case excludeColumnsString =>
            return Invalid(InvalidExcludeColumns(excludeColumnsString))
        }

    baseQueries.foreach { baseQuery =>
      if (!schemas.contains(baseQuery.table.schema)) {
        return Invalid(InvalidBaseQuerySchema(baseQuery.table.schema))
      }
    }

    extraPrimaryKeys.foreach { extraPrimaryKey =>
      if (!schemas.contains(extraPrimaryKey.table.schema)) {
        return Invalid(InvalidExtraPrimaryKeySchema(extraPrimaryKey.table.schema))
      }
    }

    extraForeignKeys.foreach { extraForeignKey =>
      if (!schemas.contains(extraForeignKey.fromTable.schema)) {
        return Invalid(InvalidExtraForeignKeySchema(extraForeignKey.fromTable.schema))
      }
      if (!schemas.contains(extraForeignKey.toTable.schema)) {
        return Invalid(InvalidExtraForeignKeySchema(extraForeignKey.toTable.schema))
      }
    }

    excludeTables.foreach { excludeTable =>
      if (!schemas.contains(excludeTable.schema)) {
        return Invalid(InvalidExcludeTableSchema(excludeTable.schema))
      }
    }

    excludeColumns.foreach { excludeColumn =>
      if (!schemas.contains(excludeColumn.table.schema)) {
        return Invalid(InvalidExcludeColumnsSchema(excludeColumn.table.schema))
      }
    }

    baseQueries.foreach { baseQuery =>
      if (excludeTables.contains(baseQuery.table)) {
        return Invalid(ExcludedTableInBaseQuery(baseQuery.table))
      }
    }

    Valid(
      SchemaConfig(
        schemas = schemas,
        baseQueries = baseQueries,
        extraPrimaryKeys = extraPrimaryKeys,
        extraForeignKeys = extraForeignKeys,
        excludeTables = excludeTables,
        excludeColumns = excludeColumns
      ),
      Config(
        originDbConnectionString = args.originDbConnectionString,
        targetDbConnectionString = args.targetDbConnectionString,
        keyCalculationDbConnectionCount = args.keyCalculationDbConnectionCount,
        dataCopyDbConnectionCount = args.dataCopyDbConnectionCount,
        tempfileStorageDirectoryOverride = args.tempfileStorageDirectoryOverride,
        runMode = args.runMode,
        metricsPort = args.metricsPort
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

  private[this] def regex(regexString: String): Regex = {
    ("""^\s*""" + regexString + """\s*$""").r
  }
}

sealed trait ExtractionResult
case class Valid(schemaConfig: SchemaConfig, config: Config) extends ExtractionResult
case class Invalid(invalidInputType: InvalidInputType) extends ExtractionResult

sealed trait InvalidInputType
case class InvalidBaseQuery(input: String) extends InvalidInputType
case class InvalidExtraPrimaryKey(input: String) extends InvalidInputType
case class DuplicateExtraPrimaryKey(tables: Set[Table]) extends InvalidInputType
case class InvalidExtraForeignKey(input: String) extends InvalidInputType
case class InvalidExcludeTable(input: String) extends InvalidInputType
case class InvalidExcludeColumns(input: String) extends InvalidInputType
case class InvalidBaseQuerySchema(schema: Schema) extends InvalidInputType
case class InvalidExtraPrimaryKeySchema(schema: Schema) extends InvalidInputType
case class InvalidExtraForeignKeySchema(schema: Schema) extends InvalidInputType
case class InvalidExcludeTableSchema(schema: Schema) extends InvalidInputType
case class InvalidExcludeColumnsSchema(schema: Schema) extends InvalidInputType
case class ExcludedTableInBaseQuery(table: Table) extends InvalidInputType
