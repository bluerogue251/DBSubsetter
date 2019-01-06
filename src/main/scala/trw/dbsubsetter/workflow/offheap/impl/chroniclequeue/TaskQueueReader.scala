package trw.dbsubsetter.workflow.offheap.impl.chroniclequeue

import java.sql.JDBCType
import java.util.UUID

import net.openhft.chronicle.wire.ValueIn
import trw.dbsubsetter.db.{DbVendor, TypeName}

private[offheap] final class TaskQueueReader(typeList: Seq[(JDBCType, TypeName)], dbVendor: DbVendor) {
  def read(in: ValueIn): Any = handlerFunc(in)

  private val handlerFunc: ValueIn => Any = {
    import DbVendor._

    val typeListWithVendors = typeList.map { case (jdbc, name) => (jdbc, name, dbVendor) }

    val funcs: Seq[ValueIn => Any] = typeListWithVendors.map {
      case (JDBCType.TINYINT | JDBCType.SMALLINT, _, MicrosoftSQLServer) =>
        (in: ValueIn) => in.int16()
      case (JDBCType.INTEGER, "INT UNSIGNED", MySQL) =>
        (in: ValueIn) => in.int64()
      case (JDBCType.TINYINT | JDBCType.SMALLINT | JDBCType.INTEGER, _, _) =>
        (in: ValueIn) => in.int32()
      case (JDBCType.BIGINT, "BIGINT UNSIGNED", MySQL) =>
        (in: ValueIn) => in.`object`()
      case (JDBCType.BIGINT, _, _) =>
        (in: ValueIn) => in.int64()
      case (JDBCType.VARCHAR | JDBCType.CHAR | JDBCType.LONGVARCHAR | JDBCType.NCHAR, _, _) =>
        (in: ValueIn) => in.text()
      case (JDBCType.BINARY | JDBCType.VARBINARY | JDBCType.LONGVARBINARY, _, _) =>
        (in: ValueIn) => in.bytes()
      case (_, "uuid", PostgreSQL) =>
        (in: ValueIn) => UUID.fromString(in.text()) // TODO optimize to use byte[] instead of string
      case (otherJDBCType, otherTypeName, _) =>
        throw new RuntimeException(s"Type not yet supported for foreign key. JDBC Type: $otherJDBCType. Type Name: $otherTypeName. Please open a GitHub issue for this.")
    }

    val headFunc: ValueIn => Any = funcs.head

    if (typeList.lengthCompare(1) == 0) {
      in: ValueIn => headFunc(in)
    } else {
      in: ValueIn => funcs.toArray.map(f => f(in))
    }
  }
}