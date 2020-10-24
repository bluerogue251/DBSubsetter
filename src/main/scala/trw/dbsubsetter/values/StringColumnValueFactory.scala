package trw.dbsubsetter.values

final class StringColumnValueFactory extends ColumnValueFactory {
  override def fromRaw(rawValue: Any): ColumnValue = {
    if (rawValue == null) {
      NullColumnValue
    } else {
      StringColumnValue(rawValue.asInstanceOf[String])
    }
  }
}
