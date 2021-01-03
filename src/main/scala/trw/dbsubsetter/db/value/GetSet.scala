package trw.dbsubsetter.db.value

import java.sql.{PreparedStatement, ResultSet}

trait GetSet {
  def get(name: String, rs: ResultSet): ColumnValue
  def set(value: ColumnValue, stmt: PreparedStatement, i: Int): Unit
}

private[this] class GetSetInt extends GetSet {
  override def get(name: String, rs: ResultSet): ColumnValue = ColumnValue.int(rs.getInt(name))
  override def set(value: ColumnValue, stmt: PreparedStatement, i: Int): Unit = stmt.setInt(i, value)
}

private[this] class GetSetLong extends GetSet {}

private[this] class GetSetBigInt extends GetSet {}

private[this] class GetSetString extends GetSet {}

private[this] class GetSetUUID extends GetSet {}

private[this] class GetSetBytes extends GetSet {}

private[this] class GetSetObject extends GetSet {}
