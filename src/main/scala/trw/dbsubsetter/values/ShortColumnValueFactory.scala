package trw.dbsubsetter.values

final class ShortColumnValueFactory extends ColumnValueFactory {
  override def fromResultSet(rawValue: Any): ColumnValue = {
    if (rawValue == null) {
      NullColumnValue
    } else {
      ShortColumnValue(rawValue.asInstanceOf[Short])
    }
  }
}
