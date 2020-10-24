package trw.dbsubsetter.values

final class BigIntColumnValueFactory extends ColumnValueFactory {
  override def fromResultSet(rawValue: Any): ColumnValue = {
    if (rawValue == null) {
      NullColumnValue
    } else {
      BigIntColumnValue(rawValue.asInstanceOf[BigInt])
    }
  }
}
