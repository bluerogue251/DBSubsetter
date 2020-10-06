package trw.dbsubsetter.config

import trw.dbsubsetter.db.{Schema, Table}

object InvalidInputMessaging {
  def toErrorMessage(invalidInputType: InvalidInputType): String = {
    invalidInputType match {
      case InvalidBaseQuery(input) =>
        invalidInputMessage("--baseQuery", input)
      case InvalidExtraPrimaryKey(input) =>
        invalidInputMessage("--primaryKey", input)
      case DuplicateExtraPrimaryKey(tables) =>
        duplicatePrimaryKeyMessage(tables)
      case InvalidExtraForeignKey(input) =>
        invalidInputMessage("--foreignKey", input)
      case InvalidExcludeTable(input) =>
        invalidInputMessage("--excludeTable", input)
      case InvalidExcludeColumns(input) =>
        invalidInputMessage("--excludeColumns", input)
      case InvalidBaseQuerySchema(schema) =>
        invalidSchema("--baseQuery", schema)
      case InvalidExtraPrimaryKeySchema(schema) =>
        invalidSchema("--primaryKey", schema)
      case InvalidExtraForeignKeySchema(schema) =>
        invalidSchema("--foreignKey", schema)
      case InvalidExcludeTableSchema(schema) =>
        invalidSchema("--excludeTable", schema)
      case InvalidExcludeColumnsSchema(schema) =>
        invalidSchema("--excludeColumns", schema)
      case ExcludedTableInBaseQuery(table) =>
        excludedTable("--baseQuery", table)

    }
  }

  private[this] def invalidInputMessage(option: String, value: String): String = {
    s"Invalid $option specified: $value."
  }

  private[this] def duplicatePrimaryKeyMessage(tables: Set[Table]): String = {
    val tablesCsv = tables.map(table => s"'${table.schema.name}.${table.name}'").mkString(", ")
    s"--primaryKey was specified more than once for table(s): $tablesCsv."
  }

  private def invalidSchema(option: String, schema: Schema): String = {
    s"""Schema '${schema.name}' was used in $option but was missing from --schemas."""
  }

  private def excludedTable(option: String, table: Table): String = {
    s"""Table '${table.schema.name}.${table.name}' specified in $option was excluded via --excludeTable."""
  }
}
