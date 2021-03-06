package trw.dbsubsetter.fktaskqueue

import net.openhft.chronicle.wire.{ValueOut, WireOut, WriteMarshallable}
import trw.dbsubsetter.chronicle.ChronicleQueueFunctions
import trw.dbsubsetter.db.ColumnTypes.ColumnType
import trw.dbsubsetter.db.ForeignKeyValue

private[fktaskqueue] final class ChronicleQueueFkTaskWriter(fkOrdinal: Int, columnTypes: Seq[ColumnType]) {
  private[this] val valueWriter: (ValueOut, ForeignKeyValue) => Unit = {
    val singleColumnWriteFunctions: Seq[(ValueOut, Any) => WireOut] =
      columnTypes.map(ChronicleQueueFunctions.singleValueWrite)

    (out, foreignKeyValue) => {
      singleColumnWriteFunctions
        .zip(foreignKeyValue.individualColumnValues)
        .foreach { case (f, singleColumnValue) => f(out, singleColumnValue) }
    }
  }

  def writeHandler(fetchChildren: Boolean, fkValue: ForeignKeyValue): WriteMarshallable =
    wireOut => {
      val out = wireOut.getValueOut
      out.bool(fetchChildren)
      out.int16(fkOrdinal)
      valueWriter(out, fkValue)
    }
}
