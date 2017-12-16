package trw.dbsubsetter.workflow

import java.sql.JDBCType
import java.util.UUID

import net.openhft.chronicle.bytes.BytesStore
import net.openhft.chronicle.wire.{ValueOut, WireOut, WriteMarshallable}
import trw.dbsubsetter.db.TypeName

class TaskQueueWriter(fkOrdinal: Short, typeList: Seq[(JDBCType, TypeName)]) {
  def writeHandler(fetchChildren: Boolean, fkValue: Any): WriteMarshallable = {
    wireOut => {
      val out = wireOut.getValueOut
      out.bool(fetchChildren)
      out.int16(fkOrdinal)
      handlerFunc(out, fkValue)
    }
  }

  private val handlerFunc: (ValueOut, Any) => Unit = {
    val funcs: Seq[(ValueOut, Any) => WireOut] = typeList.map {
      case (JDBCType.TINYINT | JDBCType.SMALLINT | JDBCType.INTEGER, _) =>
        (out: ValueOut, fkVal: Any) => out.int32(fkVal.asInstanceOf[Int])
      case (JDBCType.BIGINT, _) =>
        (out: ValueOut, fkVal: Any) => out.int64(fkVal.asInstanceOf[Long])
      case (JDBCType.CHAR | JDBCType.VARCHAR | JDBCType.LONGVARCHAR | JDBCType.NCHAR, _) =>
        (out: ValueOut, fkVal: Any) => out.text(fkVal.asInstanceOf[String])
      case (JDBCType.BINARY | JDBCType.VARBINARY | JDBCType.LONGVARBINARY, _) =>
        (out: ValueOut, fkVal: Any) =>
          val s: BytesStore[_, _] = if (fkVal == null) BytesStore.empty() else BytesStore.wrap(fkVal.asInstanceOf[Array[Byte]])
          out.bytes(s)
      case (_, "uuid") => (out: ValueOut, fkVal: Any) =>
        out.text(if (fkVal == null) null else fkVal.asInstanceOf[UUID].toString)
      case (otherJDBCType, otherTypeName) =>
        throw new RuntimeException(s"Type not yet supported for foreign key. JDBC Type: $otherJDBCType. Type Name: $otherTypeName. Please open a GitHub issue for this.")
    }

    val headFunc: (ValueOut, Any) => WireOut = funcs.head

    if (typeList.lengthCompare(1) == 0) {
      (out, fkValue) => headFunc(out, fkValue)
    } else {
      (out, fkValues) => fkValues.asInstanceOf[Array[Any]].zip(funcs).foreach { case (v, f) => f(out, v) }
    }
  }
}