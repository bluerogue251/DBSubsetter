package trw.dbsubsetter.workflow

import trw.dbsubsetter.db._

// TODO reconsider name (or the way this works) since this does not actually create any `FkTask`s. (It creates `NewTasks`).
// TODO do the same reconsideration for the Akka Streams Flow that calls this.
class FkTaskCreationWorkflow(schemaInfo: SchemaInfo) {

  def createFkTasks(pksAdded: PksAdded): NewTasks = {
    val PksAdded(table, rowsNeedingParentTasks, rowsNeedingChildTasks, viaTableOpt) = pksAdded
    val parentTasks = calcParentTasks(table, rowsNeedingParentTasks, viaTableOpt)
    val childTasks = calcChildTasks(table, rowsNeedingChildTasks)
    NewTasks(parentTasks.taskInfo ++ childTasks.taskInfo)
  }

  private def calcParentTasks(table: Table, rows: Vector[Row], viaTableOpt: Option[Table]): NewTasks = {
    // Re: `distinct`
    // It is (hopefully) a performance improvement which prevents duplicate tasks from being created
    //
    // Re: `viaTableOpt`
    // If we know that the reason we fetched a row to begin with is that it is the child of some row we've
    // already fetched, then we know that we don't need to go fetch that particular parent row again
    //
    // `distinct` and `viaTableOpt` only apply for calculating parent tasks, not child tasks.
    // Both of these seem necessary for avoiding always needing to store PKs for all parents
    //
    // `filterNot(_ == null)` should also only be necessary for parent tasks, not child tasks
    val allFks = schemaInfo.fksFromTable(table)
    val useFks = viaTableOpt.fold(allFks)(viaTable => allFks.filterNot(fk => fk.toTable == viaTable))
    val newTasksInfo: Map[(ForeignKey, Boolean), Array[Any]] = useFks.map { fk =>
      val getFkValue: Row => Any = FkTaskCreationWorkflow.fkValueExtractionFunction(fk.fromCols)
      val fkValues: Array[Any] = rows.map(getFkValue).toArray
      val distinctFkValues: Array[Any] = fkValues.distinct.filterNot(_ == null)
      (fk, false) -> distinctFkValues
    }.toMap
    NewTasks(newTasksInfo)
  }

  private def calcChildTasks(table: Table, rows: Vector[Row]): NewTasks = {
    val newTasksInfo: Map[(ForeignKey, Boolean), Array[Any]] = schemaInfo.fksToTable(table).map { fk =>
      val getFkValue: Row => Any = FkTaskCreationWorkflow.fkValueExtractionFunction(fk.toCols)
      val fkValues: Array[Any] = rows.map(getFkValue).toArray
      (fk, true) -> fkValues
    }.toMap
    NewTasks(newTasksInfo)
  }


}

private[this] object FkTaskCreationWorkflow {

  /*
   * Require IndexedSeq to ensure O(1) access for calls to `length`
   * TODO -- consider somehow moving this function onto the class itself rather than calculating it here (might
   * need to create the class, it would be called `TargetColumns` or something, representing the columns pointed
   * to from a foreign key)
   */
  private def fkValueExtractionFunction(foreignKeyColumns: IndexedSeq[Column]): Row => Any = {
    val isSingleColumnForeignKey: Boolean = foreignKeyColumns.length == 1

    if (isSingleColumnForeignKey) {
      val ordinalPosition = foreignKeyColumns.head.ordinalPosition
      val function: Row => Any = row => row(ordinalPosition)
      function
    } else {
      val ordinalPositions = foreignKeyColumns.map(_.ordinalPosition)
      val function: Row => Array[Any] = row => ordinalPositions.map(row).toArray
      function
    }
  }

}