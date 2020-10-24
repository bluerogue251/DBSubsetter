package trw.dbsubsetter.values

import java.sql.ResultSet

trait ColumnValueFactory {
  def fromBytes(bytes: Array[Byte]): ColumnValue
  def fromResultSet(resultSet: ResultSet, i: Int): ColumnValue
}

class IntColumnValueFactory extends ColumnValueFactory {
  override def fromResultSet(resultSet: ResultSet, i: Int): ColumnValue = {
    resultSet.get
    if (rawValue == null) {
      NullColumnValue
    } else {
      IntColumnValue(rawValue.asInstanceOf[Int])
    }
  }
}
