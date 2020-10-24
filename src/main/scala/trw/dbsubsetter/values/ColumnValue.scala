package trw.dbsubsetter.values

trait ColumnValue {
  def asBytes(): Array[Byte]
}
