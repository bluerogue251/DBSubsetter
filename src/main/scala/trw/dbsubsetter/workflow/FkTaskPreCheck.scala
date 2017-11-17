package trw.dbsubsetter.workflow

object FkTaskPreCheck {
  def canBePrechecked(task: FkTask): Boolean = {
    task.fk.pointsToPk && task.table == task.fk.toTable
  }
}