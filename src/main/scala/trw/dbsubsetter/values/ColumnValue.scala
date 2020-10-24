package trw.dbsubsetter.values

import java.util.UUID

sealed trait ColumnValue
case object NullColumnValue extends ColumnValue
case class ShortColumnValue(short: Short) extends ColumnValue
case class IntColumnValue(int: Int) extends ColumnValue
case class LongColumnValue(long: Long) extends ColumnValue
case class BigIntColumnValue(bigInt: BigInt) extends ColumnValue
case class StringColumnValue(string: String) extends ColumnValue
case class UUIDColumnValue(uuid: UUID) extends ColumnValue
case class ByteArrayColumnValue(bytes: Array[Byte]) extends ColumnValue
