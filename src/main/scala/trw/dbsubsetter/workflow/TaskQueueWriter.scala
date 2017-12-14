package trw.dbsubsetter.workflow

import java.sql.JDBCType

import net.openhft.chronicle.wire.{ValueOut, WireOut, WriteMarshallable}

class TaskQueueWriter(fkOrdinal: Short, typeList: Seq[JDBCType]) {
  def writeHandler(fetchChildren: Boolean, fkValue: Any): WriteMarshallable = {
    wireOut => {
      val out = wireOut.getValueOut
      out.bool(fetchChildren)
      out.int16(fkOrdinal)
      handlerFunc(out, fkValue)
    }
  }

  private val handlerFunc: (ValueOut, Any) => Unit = {
    val funcs: Array[(ValueOut, Any) => WireOut] = typeList.map {
      case JDBCType.TINYINT | JDBCType.SMALLINT => (out: ValueOut, fkVal: Any) => out.int16(fkVal.asInstanceOf[Short])
      case JDBCType.INTEGER => (out: ValueOut, fkVal: Any) => out.int32(fkVal.asInstanceOf[Int])
      case JDBCType.BIGINT => (out: ValueOut, fkVal: Any) => out.int64(fkVal.asInstanceOf[Long])
      case JDBCType.VARCHAR | JDBCType.CHAR | JDBCType.LONGVARCHAR | JDBCType.NCHAR => (out: ValueOut, fkVal: Any) => out.text(fkVal.asInstanceOf[String])
      case other => throw new RuntimeException(s"JDBC Type not yet supported for foreign key column: $other. Please open a GitHub issue for this.")
    }.toArray

    val headFunc: (ValueOut, Any) => WireOut = funcs.head

    if (typeList.lengthCompare(1) == 0) {
      (out, fkValue) => headFunc(out, fkValue)
    } else {
      (out, fkValues) => fkValues.asInstanceOf[Array[Any]].zip(funcs).foreach { case (v, f) => f(out, v) }
    }
  }
}