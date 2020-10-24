package trw.dbsubsetter.values

final class StringColumnValueFactory extends ColumnValueFactory {
  override def fromResultSet(rawValue: Any): ColumnValue = {
    if (rawValue == null) {
      NullColumnValue
    } else {
      BigIntColumnValue(rawValue.asInstanceOf[String])
    }
  }
}
