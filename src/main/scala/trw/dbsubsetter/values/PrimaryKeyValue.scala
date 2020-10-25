package trw.dbsubsetter.values

sealed trait PrimaryKeyValue
case class SingleColumnPrimaryKeyValue(value: ColumnValue) extends PrimaryKeyValue
case class MultiColumnPrimaryKeyValue(values: Seq[ColumnValue]) extends PrimaryKeyValue
