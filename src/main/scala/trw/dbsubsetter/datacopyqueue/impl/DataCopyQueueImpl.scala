package trw.dbsubsetter.datacopyqueue.impl

import trw.dbsubsetter.config.Config
import trw.dbsubsetter.datacopyqueue.DataCopyQueue
import trw.dbsubsetter.db.ColumnTypes.ColumnType
import trw.dbsubsetter.db.{Row, SchemaInfo, Table}
import trw.dbsubsetter.workflow.PksAdded

import scala.collection.mutable


/**
  * WARNING: this class is not threadsafe
  */
private[datacopyqueue] final class DataCopyQueueImpl(config: Config, schemaInfo: SchemaInfo) extends DataCopyQueue {

  private[this] val tablesToQueuedValueCounts: mutable.Map[Table, Long] = {
    val elems: Seq[(Table, Long)] =
      schemaInfo.
        tablesByName
        .map { case ((_, _), table) => table -> 0L }
        .toSeq

    mutable.Map[Table, Long](elems: _*)
  }

  private[this] val tablesWithQueuedValues: mutable.Set[Table] = new mutable.HashSet[Table]()

  private[this] val tablesToChronicleQueues: Map[Table, ChronicleQueueAccess] = {
    schemaInfo
      .pksByTableOrdered
      .map { case (table, primaryKeyColumns) =>
          val columnTypes: Seq[ColumnType] = primaryKeyColumns.map(_.dataType)
          table -> new ChronicleQueueAccess(config, columnTypes)
      }
  }

  override def enqueue(pksAdded: PksAdded): Unit = {
    val pkValues: Vector[Row] = pksAdded.rowsNeedingParentTasks

    // TODO centralize this nonEmpty check somewhere else
    if (pkValues.nonEmpty) {
      val table: Table = pksAdded.table
      val chronicleQueueAccess: ChronicleQueueAccess = tablesToChronicleQueues(table)
      chronicleQueueAccess.write()

      val previousCount: Long = tablesToQueuedValueCounts(table)
      tablesToQueuedValueCounts.update(table, previousCount + pkValues.size)
      tablesWithQueuedValues.add(table)
    }

    val runnable: Runnable = () => delegatee.enqueue(pksAdded)
    taskEnqueueDuration.time(runnable)
    pendingTaskCount.inc(pksAdded.rowsNeedingParentTasks.length)
  }

  override def dequeue(): Option[PksAdded] = {
    if (tablesWithQueuedValues.isEmpty) {
      None
    } else {
      val table: Table = tablesWithQueuedValues.head

    }
  }
}
