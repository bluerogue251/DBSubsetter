package trw.dbsubsetter.workflow

object FkTaskPreCheck {
  def canPrecheck(task: FkTask): Boolean = {
    task.fk.pointsToPk && task.table == task.fk.toTable
  }
}