package trw.dbsubsetter.config

object InvalidInputMessaging {
  def toErrorMessage(invalidInputType: InvalidInputType): String = {
    invalidInputType match {
      case InvalidBaseQuery(input) =>
        invalidInputMessage("--baseQuery", input)
      case InvalidExtraPrimaryKey(input) =>
        invalidInputMessage("--primaryKey", input)
      case InvalidExtraForeignKey(input) =>
        invalidInputMessage("--foreignKey", input)
      case InvalidExcludeTable(input) =>
        invalidInputMessage("--excludeTable", input)
      case InvalidExcludeColumns(input) =>
        invalidInputMessage("--excludeColumns", input)
      case InvalidBaseQuerySchema(table) =>
        s"""Schema '${table.schema.name}' was used in --baseQuery but was missing from --schemas."""
    }
  }

  private[this] def invalidInputMessage(option: String, value: String): String = {
    s"Invalid $option specified: $value."
  }
}
