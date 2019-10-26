package trw.dbsubsetter.workflow.offheap.impl.chroniclequeue

import java.math.BigInteger
import java.sql.JDBCType
import java.util.UUID

import net.openhft.chronicle.wire.{ValueOut, WireOut, WriteMarshallable}
import trw.dbsubsetter.db.{DbVendor, TypeName}

private[offheap] final class TaskQueueWriter(fkOrdinal: Short, typeList: Seq[(JDBCType, TypeName)], dbVendor: DbVendor) {
  def writeHandler(fetchChildren: Boolean, fkValue: Any): WriteMarshallable = {
    wireOut => {
      val out = wireOut.getValueOut
      out.bool(fetchChildren)
      out.int16(fkOrdinal)
      handlerFunc(out, fkValue)
    }
  }

  private val handlerFunc: (ValueOut, Any) => Unit = {
    import DbVendor._
    val typeListWithVendors = typeList.map { case (jdbc, name) => (jdbc, name, dbVendor) }

    val funcs: Seq[(ValueOut, Any) => WireOut] = typeListWithVendors.map {
      case (JDBCType.TINYINT | JDBCType.SMALLINT, _, MicrosoftSQLServer) =>
        (out: ValueOut, fkVal: Any) => out.int16(fkVal.asInstanceOf[Short])
      case (JDBCType.INTEGER, "INT UNSIGNED", MySQL) =>
        (out: ValueOut, fkVal: Any) => out.int64(fkVal.asInstanceOf[Long])
      case (JDBCType.TINYINT | JDBCType.SMALLINT | JDBCType.INTEGER, _, _) =>
        (out: ValueOut, fkVal: Any) => out.int32(fkVal.asInstanceOf[Int])
      case (JDBCType.BIGINT, "BIGINT UNSIGNED", MySQL) =>
        (out: ValueOut, fkVal: Any) => out.`object`(fkVal.asInstanceOf[BigInteger])
      case (JDBCType.BIGINT, _, _) =>
        (out: ValueOut, fkVal: Any) => out.int64(fkVal.asInstanceOf[Long])
      case (JDBCType.CHAR | JDBCType.VARCHAR | JDBCType.LONGVARCHAR | JDBCType.NCHAR, _, _) =>
        (out: ValueOut, fkVal: Any) => out.text(fkVal.asInstanceOf[String])
      case (JDBCType.BINARY | JDBCType.VARBINARY | JDBCType.LONGVARBINARY, _, _) =>
        (out: ValueOut, fkVal: Any) => out.bytes(fkVal.asInstanceOf[Array[Byte]])
      case (_, "uuid", PostgreSQL) =>
        (out: ValueOut, fkVal: Any) => out.text(fkVal.asInstanceOf[UUID].toString) // TODO optimize to use byte[] instead of string
      case (otherJDBCType, otherTypeName, _) =>
        throw new RuntimeException(s"Type not yet supported for foreign key. JDBC Type: $otherJDBCType. Type Name: $otherTypeName. Please open a GitHub issue for this.")
    }

    if (typeList.lengthCompare(1) == 0) {
      (out, fkValue) => funcs.head(out, fkValue)
    } else {
      (out, fkValues) => fkValues.asInstanceOf[Array[Any]].zip(funcs).foreach { case (v, f) => f(out, v) }
    }
  }
}
