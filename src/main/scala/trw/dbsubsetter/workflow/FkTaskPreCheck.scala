package trw.dbsubsetter.workflow

object FkTaskPreCheck {
  def shouldPrecheck(task: FetchParentTask): Boolean = {
    task.fk.pointsToPk && task.table.storePks
  }
}