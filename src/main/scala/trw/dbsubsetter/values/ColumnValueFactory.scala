package trw.dbsubsetter.values

trait ColumnValueFactory {
  def fromRaw(raw: Any): ColumnValue
}
