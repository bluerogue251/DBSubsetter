package trw.dbsubsetter.datacopyqueue.impl

import trw.dbsubsetter.config.Config
import trw.dbsubsetter.datacopyqueue.DataCopyQueue
import trw.dbsubsetter.db.ColumnTypes.ColumnType
import trw.dbsubsetter.db.{Constants, PrimaryKeyValue, Row, SchemaInfo, Table}
import trw.dbsubsetter.workflow.{DataCopyTask, PksAdded}

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

  private[this] val pkValueExtractionFunctions: Map[Table, Row => PrimaryKeyValue] =
    DataCopyQueueImpl.buildPkValueExtractionFunctions(schemaInfo)

  override def enqueue(pksAdded: PksAdded): Unit = {
    val rows: Vector[Row] = pksAdded.rowsNeedingParentTasks

    if (rows.nonEmpty) {
      val table: Table = pksAdded.table
      val chronicleQueueAccess: ChronicleQueueAccess = tablesToChronicleQueues(table)
      val extractPkValue = pkValueExtractionFunctions(table)
      val pkValues: Seq[PrimaryKeyValue] = rows.map(extractPkValue)

      chronicleQueueAccess.write(pkValues)

      val previousCount: Long = tablesToQueuedValueCounts(table)
      tablesToQueuedValueCounts.update(table, previousCount + pkValues.size)
      tablesWithQueuedValues.add(table)
    }
  }

  override def dequeue(): Option[DataCopyTask] = {
    if (tablesWithQueuedValues.isEmpty) {
      None
    } else {
      // TODO would be nice to eventually make this stuff constant time
      val table: Table = tablesWithQueuedValues.maxBy(tablesToQueuedValueCounts)
      val totalQuantityAvailable: Long = tablesToQueuedValueCounts(table)
      val dequeueQuantity = Constants.dataCopyBatchSizes.filter(_ <= totalQuantityAvailable).max

      val chronicleQueueAccess: ChronicleQueueAccess = tablesToChronicleQueues(table)
      val primaryKeyValues: Seq[PrimaryKeyValue] = chronicleQueueAccess.read(dequeueQuantity)
      val dataCopyTask = new DataCopyTask(table, primaryKeyValues)

      tablesToQueuedValueCounts.update(table, totalQuantityAvailable - dequeueQuantity)
      if (totalQuantityAvailable == dequeueQuantity) {
        tablesWithQueuedValues.remove(table)
      }

      Some(dataCopyTask)
    }
  }

  override def isEmpty(): Boolean = {
    tablesWithQueuedValues.isEmpty
  }
}

object DataCopyQueueImpl {

  // TODO consider deduplication with similar logic in PkStoreWorkflow and ApplicationSingleThreaded
  private def buildPkValueExtractionFunctions(schemaInfo: SchemaInfo): Map[Table, Row => PrimaryKeyValue] = {
    schemaInfo.pksByTableOrdered.map { case (table, pkColumns) =>
      val primaryKeyColumnOrdinals: Vector[Int] = pkColumns.map(_.ordinalPosition)
      val primaryKeyExtractionFunction: Row => PrimaryKeyValue = row => {
        val individualColumnValues: Seq[Any] = primaryKeyColumnOrdinals.map(row)
        new PrimaryKeyValue(individualColumnValues)
      }
      table -> primaryKeyExtractionFunction
    }
  }
}
