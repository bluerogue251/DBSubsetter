package trw.dbsubsetter.workflow

import trw.dbsubsetter.db._
import trw.dbsubsetter.keyextraction.KeyExtractionUtil


// TODO reconsider name (or the way this works) since this does not actually create any `FkTask`s. (It creates `NewTasks`).
// TODO do the same reconsideration for the Akka Streams Flow that calls this.
final class FkTaskCreationWorkflow(schemaInfo: SchemaInfo) {

  private[this] val fkExtractionFunctions: Map[(ForeignKey, Boolean), Row => ForeignKeyValue] =
    KeyExtractionUtil.fkExtractionFunctions(schemaInfo)

  def createFkTasks(pksAdded: PksAdded): NewTasks = {
    val PksAdded(table, rowsNeedingParentTasks, rowsNeedingChildTasks, viaTableOpt) = pksAdded
    val parentTasks = calcParentTasks(table, rowsNeedingParentTasks, viaTableOpt)
    val childTasks = calcChildTasks(table, rowsNeedingChildTasks)
    NewTasks(parentTasks.taskInfo ++ childTasks.taskInfo)
  }

  private[this] def calcParentTasks(table: Table, rows: Vector[Row], viaTableOpt: Option[Table]): NewTasks = {
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
    // `filterNot(_.isEmpty)` should also only be necessary for parent tasks, not child tasks
    val allForeignKeys = schemaInfo.fksFromTable(table)
    val useForeignKeys = viaTableOpt.fold(allForeignKeys)(viaTable => allForeignKeys.filterNot(fk => fk.toTable == viaTable))
    val newTasksInfo: Map[(ForeignKey, Boolean), Seq[ForeignKeyValue]] =
      useForeignKeys.map { fk =>
        val fkValueExtractionFunction: Row => ForeignKeyValue = fkExtractionFunctions(fk, false)
        val fkValues: Seq[ForeignKeyValue] = rows.map(fkValueExtractionFunction).filterNot(_.isEmpty)
        (fk, false) -> fkValues
      }.toMap
    NewTasks(newTasksInfo)
  }

  private[this] def calcChildTasks(table: Table, rows: Vector[Row]): NewTasks = {
    val allForeignKeys = schemaInfo.fksToTable(table)
    val newTasksInfo: Map[(ForeignKey, Boolean), Seq[ForeignKeyValue]] = allForeignKeys.map { fk =>
      val fkValueExtractionFunction: Row => ForeignKeyValue = fkExtractionFunctions(fk, true)
      val fkValues: Seq[ForeignKeyValue] = rows.map(fkValueExtractionFunction)
      (fk, true) -> fkValues
    }.toMap
    NewTasks(newTasksInfo)
  }
}
