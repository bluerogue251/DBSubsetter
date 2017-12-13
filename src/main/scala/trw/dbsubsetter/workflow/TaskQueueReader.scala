package trw.dbsubsetter.workflow

import java.sql.JDBCType

import net.openhft.chronicle.wire.ValueIn

class TaskQueueReader(typeList: Array[JDBCType]) {
  def read(in: ValueIn): Array[Any] = handlerFunc(in)

  private val handlerFunc: ValueIn => Array[Any] = {
    val funcs: Array[ValueIn => Any] = typeList.map {
      case JDBCType.TINYINT | JDBCType.SMALLINT => (in: ValueIn) => in.int16()
      case JDBCType.INTEGER => (in: ValueIn) => in.int32()
      case JDBCType.BIGINT => (in: ValueIn) => in.int64()
      case JDBCType.VARCHAR | JDBCType.CHAR | JDBCType.LONGVARCHAR | JDBCType.NCHAR => (in: ValueIn) => in.text()
      case other => throw new RuntimeException(s"JDBC Type not yet supported for foreign key column: $other. Please open a GitHub issue for this.")
    }

    val headFunc: ValueIn => Any = funcs.head

    if (typeList.lengthCompare(1) == 0) {
      (in: ValueIn) => Array[Any](headFunc(in))
    } else {
      (in: ValueIn) => funcs.map(f => f(in))
    }
  }
}