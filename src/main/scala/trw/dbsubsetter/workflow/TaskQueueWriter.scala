package trw.dbsubsetter.workflow

import java.sql.JDBCType

import net.openhft.chronicle.wire.{WireOut, WriteMarshallable}

class TaskQueueWriter {

  private def getWriteHandler(typeList: List[JDBCType]): (Boolean, Boolean, Array[Any]) => WriteMarshallable = {
    (isCompleted, fetchChildren, fkValues) => {
      wireOut => {
        if (isCompleted) {
          wireOut.getValueOut.bool(true)
        } else {
          val start = wireOut.getValueOut.bool(false).getValueOut.bool(fetchChildren)
          typeList.zipWithIndex.foldLeft[WireOut](start) { case (wo, (sqlType, i)) =>
            sqlType match {
              case JDBCType.TINYINT | JDBCType.SMALLINT => wo.getValueOut.int16(fkValues(i).asInstanceOf[Short])
              case JDBCType.INTEGER => wo.getValueOut.int32(fkValues(i).asInstanceOf[Int])
              case JDBCType.BIGINT => wo.getValueOut.int64(fkValues(i).asInstanceOf[Long])
              case JDBCType.VARCHAR | JDBCType.CHAR | JDBCType.LONGVARCHAR | JDBCType.NCHAR => wo.getValueOut.text(fkValues(i).asInstanceOf[String])
            }
          }
        }
      }
    }
  }
}