package trw.dbsubsetter.values

final class IntColumnValueFactory extends ColumnValueFactory {
  override def fromRaw(rawValue: Any): ColumnValue = {
    if (rawValue == null) {
      NullColumnValue
    } else {
      ShortColumnValue(rawValue.asInstanceOf[Int])
    }
  }
}
