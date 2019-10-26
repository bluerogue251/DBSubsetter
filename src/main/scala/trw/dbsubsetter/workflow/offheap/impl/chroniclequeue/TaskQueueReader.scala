package trw.dbsubsetter.workflow.offheap.impl.chroniclequeue

import net.openhft.chronicle.wire.ValueIn
import trw.dbsubsetter.db.ColumnTypes.ColumnType


private[offheap] final class TaskQueueReader(columnTypes: Seq[ColumnType]) {
  private[this] val valueReader: ValueIn => Any = {
    val funcs: Seq[ValueIn => Any] =
      columnTypes.map(ChronicleQueueFunctions.singleValueRead)

    if (columnTypes.size == 1) {
      in: ValueIn => funcs.head(in)
    } else {
      in: ValueIn => funcs.toArray.map(f => f(in))
    }
  }

  def read(in: ValueIn): Any = {
    valueReader(in)
  }
}
