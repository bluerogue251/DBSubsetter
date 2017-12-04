package trw.dbsubsetter.workflow

object FkTaskPreCheck {
  def needsPrecheck(task: FkTask): Boolean = {
    task.fk.pointsToPk && task.table == task.fk.toTable && task.table.storePks
  }
}