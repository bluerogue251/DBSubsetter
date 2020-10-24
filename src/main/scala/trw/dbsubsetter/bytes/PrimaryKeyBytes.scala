package trw.dbsubsetter.bytes

import trw.dbsubsetter.db._

object PrimaryKeyBytes {
  def toBytes(primaryKeyValue: PrimaryKeyValue): Array[Byte] = {
    primaryKeyValue match {
      case SingleColumnPrimaryKeyValue(value) => ColumnBytes.toBytes(value)
      case MultiColumnPrimaryKeyValue(values) => MultiColumnBytes.toBytes(values)
    }
  }
}
