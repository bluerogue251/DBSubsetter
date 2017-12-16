package trw.dbsubsetter.workflow

import java.sql.JDBCType
import java.util.UUID

import net.openhft.chronicle.wire.ValueIn
import trw.dbsubsetter.db.TypeName

class TaskQueueReader(typeList: Seq[(JDBCType, TypeName)]) {
  def read(in: ValueIn): Any = handlerFunc(in)

  private val handlerFunc: ValueIn => Any = {
    val funcs: Seq[ValueIn => Any] = typeList.map {
      case (JDBCType.TINYINT | JDBCType.SMALLINT | JDBCType.INTEGER, _) =>
        (in: ValueIn) => in.int32()
      case (JDBCType.BIGINT, _) =>
        (in: ValueIn) => in.int64()
      case (JDBCType.VARCHAR | JDBCType.CHAR | JDBCType.LONGVARCHAR | JDBCType.NCHAR, _) =>
        (in: ValueIn) => in.text()
      case (JDBCType.BINARY | JDBCType.VARBINARY | JDBCType.LONGVARBINARY, _) =>
        (in: ValueIn) => in.bytes()
      case (_, "uuid") =>
        (in: ValueIn) => UUID.fromString(in.text()) // TODO optimize to use byte[] instead of string
      case other =>
        throw new RuntimeException(s"JDBC Type not yet supported for foreign key column: $other. Please open a GitHub issue for this.")
    }

    val headFunc: ValueIn => Any = funcs.head

    if (typeList.lengthCompare(1) == 0) {
      (in: ValueIn) => headFunc(in)
    } else {
      (in: ValueIn) => funcs.toArray.map(f => f(in))
    }
  }
}