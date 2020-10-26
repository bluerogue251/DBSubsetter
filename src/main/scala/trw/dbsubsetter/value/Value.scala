package trw.dbsubsetter.value

import java.math.BigInteger
import java.util.UUID

sealed trait Value
case class ByteValue(byte: Byte) extends Value
case class ShortValue(short: Short) extends Value
case class IntValue(int: Int) extends Value
case class LongValue(long: Long) extends Value
case class BigIntValue(bigInt: BigInt) extends Value
case class BigIntegerValue(bigInteger: BigInteger) extends Value
case class StringValue(string: String) extends Value
case class UUIDValue(uuid: UUID) extends Value
case class ByteArrayValue(bytes: Array[Byte]) extends Value
case class MultiValue(members: Seq[Value]) extends Value
