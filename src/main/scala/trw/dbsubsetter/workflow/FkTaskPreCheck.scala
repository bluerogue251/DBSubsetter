package trw.dbsubsetter.workflow

object FkTaskPreCheck {
  def shouldPrecheck(task: FkTask): Boolean = {
    task.fk.pointsToPk && task.table.storePks && task.table == task.fk.toTable
  }
}