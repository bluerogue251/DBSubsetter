package trw.dbsubsetter.db


// TODO hide this and other TargetDB-related things as implementation details of DataCopyWorkflow
trait TargetDbAccess {
  def insertRows(table: Table, rows: Vector[Row]): Unit
}
