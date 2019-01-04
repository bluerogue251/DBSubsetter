package trw.dbsubsetter.workflow

object TaskPreCheck {
  def shouldPrecheck(originDbRequest: OriginDbRequest): Boolean = {
    originDbRequest match {
      case FetchParentTask(foreignKey, _) => foreignKey.pointsToPk && foreignKey.toTable.storePks
      case _ => false
    }
  }
}