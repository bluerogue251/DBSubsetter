package trw.dbsubsetter.values

trait ColumnValueFactory {
  def fromBytes(bytes: Array[Byte]): ColumnValue
}
