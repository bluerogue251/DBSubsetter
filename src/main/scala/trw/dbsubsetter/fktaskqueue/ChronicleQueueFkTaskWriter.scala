package trw.dbsubsetter.fktaskqueue

import net.openhft.chronicle.wire.WriteMarshallable
import trw.dbsubsetter.db.ForeignKeyValue

private[fktaskqueue] final class ChronicleQueueFkTaskWriter(fkOrdinal: Short) {
  def writeHandler(fetchChildren: Boolean, fkValue: ForeignKeyValue): WriteMarshallable =
    wireOut => {
      val out = wireOut.getValueOut
      out.bool(fetchChildren)
      out.int16(fkOrdinal)
      fkValue.individualColumnValues.foreach { columnValueBytes =>
        out.bytes(columnValueBytes)
      }
    }
}
