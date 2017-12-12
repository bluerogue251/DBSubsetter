package trw.dbsubsetter.workflow

import java.sql.JDBCType

import net.openhft.chronicle.wire.ValueIn

class TaskQueueReader(typeList: Array[JDBCType]) {
  // TODO it seems like we recalculate / retype-check way more than we should need to here?
  def read(in: ValueIn): Array[Any] = {
    typeList.map {
      case JDBCType.TINYINT | JDBCType.SMALLINT => in.int16()
      case JDBCType.INTEGER => in.int32()
      case JDBCType.BIGINT => in.int64()
      case JDBCType.VARCHAR | JDBCType.CHAR | JDBCType.LONGVARCHAR | JDBCType.NCHAR => in.text()
      case other => throw new RuntimeException(s"JDBC Type not yet supported for foreign key column: $other. Please open a GitHub issue for this.")
    }
  }
}