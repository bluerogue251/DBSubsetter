package trw.dbsubsetter.fktaskqueue.impl

import net.openhft.chronicle.wire.ValueOut
import net.openhft.chronicle.wire.WireOut
import net.openhft.chronicle.wire.WriteMarshallable
import trw.dbsubsetter.chronicle.ChronicleQueueFunctions
import trw.dbsubsetter.db.ColumnTypes.ColumnType
import trw.dbsubsetter.db.ForeignKeyValue

private[impl] final class ChronicleQueueFkTaskWriter(fkOrdinal: Short, columnTypes: Seq[ColumnType]) {
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
