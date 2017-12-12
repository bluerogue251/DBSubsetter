package trw.dbsubsetter.workflow

import java.sql.JDBCType

import net.openhft.chronicle.wire.{ReadMarshallable, WireIn}

class TaskQueueReader {
  var current: (Boolean, Boolean, Array[Any]) = _

  private def getReadHandler(typeList: List[JDBCType]): ReadMarshallable = {
    (wi: WireIn) => {
      val in = wi.getValueIn
      val isCompleted = in.bool()

      if (isCompleted) {
        current = (true, false, null)
      } else {
        val fetchChildren = in.bool()
        val tmp = Array.fill[Any](typeList.size)(null)
        typeList.zipWithIndex.foreach { case (jdbcType, i) =>
          tmp(i) = jdbcType match {
            case JDBCType.TINYINT | JDBCType.SMALLINT => in.int16()
            case JDBCType.INTEGER => in.int32()
            case JDBCType.BIGINT => in.int64()
            case JDBCType.VARCHAR | JDBCType.CHAR | JDBCType.LONGVARCHAR | JDBCType.NCHAR => in.text()
            case other => throw new RuntimeException(s"JDBC Type not yet supported for foreign key column: $other. Please open a GitHub issue for this.")
          }
        }
        current = (false, fetchChildren, tmp)
      }
    }
  }
}