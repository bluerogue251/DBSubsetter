package trw.dbsubsetter.workflow

import java.sql.JDBCType
import java.util.UUID

import net.openhft.chronicle.wire.ValueIn
import trw.dbsubsetter.db.TypeName

class TaskQueueReader(typeList: Seq[(JDBCType, TypeName)]) {
  def read(in: ValueIn): Any = handlerFunc(in)

  private val handlerFunc: ValueIn => Any = {
    val funcs: Seq[ValueIn => Any] = typeList.map {
      case (JDBCType.TINYINT | JDBCType.SMALLINT, _) => (in: ValueIn) => in.int16()
      case (JDBCType.INTEGER, _) => (in: ValueIn) => in.int32()
      case (JDBCType.BIGINT, _) => (in: ValueIn) => in.int64()
      case (JDBCType.VARCHAR | JDBCType.CHAR | JDBCType.LONGVARCHAR | JDBCType.NCHAR, _) => (in: ValueIn) => in.text()
      case (_, "uuid") => (in: ValueIn) => val t = in.text(); if (t == null) null else UUID.fromString(t)
      case other => throw new RuntimeException(s"JDBC Type not yet supported for foreign key column: $other. Please open a GitHub issue for this.")
    }

    val headFunc: ValueIn => Any = funcs.head

    if (typeList.lengthCompare(1) == 0) {
      (in: ValueIn) => headFunc(in)
    } else {
      (in: ValueIn) => funcs.map(f => f(in))
    }
  }
}