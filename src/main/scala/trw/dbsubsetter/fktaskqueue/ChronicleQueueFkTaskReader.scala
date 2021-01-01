package trw.dbsubsetter.fktaskqueue

import net.openhft.chronicle.wire.ValueIn
import trw.dbsubsetter.db.ColumnTypes.ColumnType
import trw.dbsubsetter.db.ForeignKeyValue

private[fktaskqueue] final class ChronicleQueueFkTaskReader(columnTypes: Seq[ColumnType]) {
  private[this] val valueReader: ValueIn => ForeignKeyValue = {
    val singleColumnReadFunctions: Seq[ValueIn => Any] =
      columnTypes.map(ChronicleQueueFunctions.singleValueRead)

    in: ValueIn => {
      val individualColumnValues: Seq[Any] = singleColumnReadFunctions.map(f => f(in))
      new ForeignKeyValue(individualColumnValues)
    }
  }

  def read(in: ValueIn): ForeignKeyValue = {
    valueReader(in)
  }
}
