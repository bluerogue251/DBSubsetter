package trw.dbsubsetter.workflow.offheap.impl.chroniclequeue

import net.openhft.chronicle.wire.{ValueOut, WireOut, WriteMarshallable}
import trw.dbsubsetter.db.ColumnTypes.ColumnType


private[offheap] final class TaskQueueWriter(fkOrdinal: Short, columnTypes: Seq[ColumnType]) {
  private[this] val valueWriter: (ValueOut, Any) => Unit = {
    val funcs: Seq[(ValueOut, Any) => WireOut] =
      columnTypes.map(ChronicleQueueFunctions.resolveWriteFunction)

    if (columnTypes.size == 1) {
      (out, fkValue) => funcs.head(out, fkValue)
    } else {
      (out, fkValues) => fkValues.asInstanceOf[Array[Any]].zip(funcs).foreach { case (v, f) => f(out, v) }
    }
  }

  def writeHandler(fetchChildren: Boolean, fkValue: Any): WriteMarshallable = {
    wireOut => {
      val out = wireOut.getValueOut
      out.bool(fetchChildren)
      out.int16(fkOrdinal)
      valueWriter(out, fkValue)
    }
  }
}
