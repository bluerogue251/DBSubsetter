package trw.dbsubsetter.workflow.offheap.impl.chroniclequeue

import net.openhft.chronicle.wire.ValueIn
import trw.dbsubsetter.db.ColumnTypes
import trw.dbsubsetter.db.ColumnTypes.ColumnType

private[offheap] final class TaskQueueReader(columnTypes: Seq[ColumnType]) {

  def read(in: ValueIn): Any = {
    valueReader(in)
  }

  private val valueReader: ValueIn => Any = {
    val funcs: Seq[ValueIn => Any] =
      columnTypes.map {
        case ColumnTypes.Short =>
          (in: ValueIn) => in.int16()
        case ColumnTypes.Int =>
          (in: ValueIn) => in.int32()
        case ColumnTypes.Long =>
          (in: ValueIn) => in.int64()
        case ColumnTypes.BigInteger =>
          (in: ValueIn) => in.`object`()
        case ColumnTypes.String =>
          (in: ValueIn) => in.text()
        case ColumnTypes.ByteArray =>
          (in: ValueIn) => in.bytes()
        case ColumnTypes.Uuid =>
          (in: ValueIn) => in.uuid()
        case ColumnTypes.Unknown(description) =>
          val errorMessage =
            s"Column type not yet fully supported: $description. " +
              "Please open a GitHub issue and we will try to address it promptly."
          throw new RuntimeException(errorMessage)
      }

    if (columnTypes.size == 1) {
      in: ValueIn => funcs.head(in)
    } else {
      in: ValueIn => funcs.toArray.map(f => f(in))
    }
  }
}
