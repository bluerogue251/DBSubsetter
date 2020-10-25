package trw.dbsubsetter.values

sealed trait ColumnValue

case object NullColumnValue extends ColumnValue
case class ShortColumnValue(bytes: Array[Byte]) extends ColumnValue
case class IntColumnValue(bytes: Array[Byte]) extends ColumnValue
case class LongColumnValue(bytes: Array[Byte]) extends ColumnValue
case class BigIntColumnValue(bytes: Array[Byte]) extends ColumnValue
case class StringColumnValue(bytes: Array[Byte]) extends ColumnValue
case class UUIDColumnValue(bytes: Array[Byte]) extends ColumnValue
case class ByteArrayColumnValue(bytes: Array[Byte]) extends ColumnValue
