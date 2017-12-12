package trw.dbsubsetter.workflow

import java.sql.JDBCType

import net.openhft.chronicle.wire.WriteMarshallable

class TaskQueueWriter(fkOrdinal: Short, typeList: Seq[JDBCType]) {
  val writeHandler: (Boolean, Array[Any]) => WriteMarshallable = {
    (fetchChildren, fkValues) => {
      wireOut => {
        val out = wireOut.getValueOut
        out.bool(false) // completed = false
        out.bool(fetchChildren)
        out.int16(fkOrdinal)
        typeList.zipWithIndex.foreach { case (sqlType, i) =>
          sqlType match {
            case JDBCType.TINYINT | JDBCType.SMALLINT => out.int16(fkValues(i).asInstanceOf[Short])
            case JDBCType.INTEGER => out.int32(fkValues(i).asInstanceOf[Int])
            case JDBCType.BIGINT => out.int64(fkValues(i).asInstanceOf[Long])
            case JDBCType.VARCHAR | JDBCType.CHAR | JDBCType.LONGVARCHAR | JDBCType.NCHAR => out.text(fkValues(i).asInstanceOf[String])
            case other => throw new RuntimeException(s"JDBC Type not yet supported for foreign key column: $other. Please open a GitHub issue for this.")
          }
        }
      }
    }
  }
}