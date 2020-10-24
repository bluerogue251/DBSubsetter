package trw.dbsubsetter.values

final class LongColumnValueFactory extends ColumnValueFactory {
  override def fromRaw(rawValue: Any): ColumnValue = {
    if (rawValue == null) {
      NullColumnValue
    } else {
      LongColumnValue(rawValue.asInstanceOf[Long])
    }
  }
}
