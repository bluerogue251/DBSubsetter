package trw.dbsubsetter.workflow

object FkTaskPreCheck {
  // Check if we can skip this task since we've already seen this row already (if applicable)
  // TODO -- think about and write comment about why this only applies to `FetchParentTask` and not `FetchChildrenTask`
  // Maybe there _is_ a use case for checking for a `FetchChildrenTask` --> if it's a one-to-one relationship and the child table shares the same PK value
  // with the parent table, it seems in that case it might work to do a Precheck? (In most cases, the child table's value will _not_ be it's PK, so in most
  // cases the PKStore can't help us... but in the one-to-one case where the child column happens to be its PrimaryKey, it might work
  def shouldPrecheck(task: FetchParentTask): Boolean = {
    task.fk.pointsToPk && task.table.storePks
  }
}