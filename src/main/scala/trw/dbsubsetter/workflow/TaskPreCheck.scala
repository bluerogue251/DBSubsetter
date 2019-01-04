package trw.dbsubsetter.workflow

object TaskPreCheck {
  def shouldPrecheck(fetchParentTask: FetchParentTask): Boolean = {
    fetchParentTask.foreignKey.pointsToPk && fetchParentTask.foreignKey.toTable.storePks
  }
}