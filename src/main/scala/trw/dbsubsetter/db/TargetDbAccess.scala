package trw.dbsubsetter.db

trait TargetDbAccess {
  def insertRows(table: Table, rows: Vector[Row]): Unit
}
