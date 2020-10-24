package trw.dbsubsetter.datacopy

import java.nio.file.{Path, Paths}

import trw.dbsubsetter.db.{Constants, Keys, MultiColumnPrimaryKeyValue, SchemaInfo, Table}
import trw.dbsubsetter.keyextraction.KeyExtractionUtil
import trw.dbsubsetter.pkstore.PksAdded

import scala.collection.mutable

private[datacopy] final class DataCopyQueueImpl(storageDirectory: Path, schemaInfo: SchemaInfo) extends DataCopyQueue {

  private[this] val tablesToQueuedValueCounts: mutable.Map[Table, Long] = {
    val elems: Seq[(Table, Long)] =
      schemaInfo.tables
        .map { tableWithMetadata =>
          tableWithMetadata.table -> 0L
        }

    mutable.Map[Table, Long](elems: _*)
  }

  private[this] val tablesWithQueuedValues: mutable.Set[Table] = new mutable.HashSet[Table]()

  private[this] val tablesToChronicleQueues: Map[Table, ChronicleQueueAccess] = {
    schemaInfo.pksByTable.zipWithIndex
      .map { case ((table, primaryKey), i) =>
        val tableSubdirectory = Paths.get(storageDirectory.toString, s"t$i")
        table -> new ChronicleQueueAccess(tableSubdirectory, primaryKey)
      }
  }

  private[this] val pkValueExtractionFunctions: Map[Table, Keys => MultiColumnPrimaryKeyValue] =
    KeyExtractionUtil.pkExtractionFunctions(schemaInfo)

  override def enqueue(pksAdded: PksAdded): Unit = {
    val rows: Vector[Keys] = pksAdded.rowsNeedingParentTasks

    if (rows.nonEmpty) {
      val table: Table = pksAdded.table
      val chronicleQueueAccess: ChronicleQueueAccess = tablesToChronicleQueues(table)
      val extractPkValue = pkValueExtractionFunctions(table)
      val pkValues: Seq[MultiColumnPrimaryKeyValue] = rows.map(extractPkValue)

      this.synchronized {
        chronicleQueueAccess.write(pkValues)
        val previousCount: Long = tablesToQueuedValueCounts(table)
        tablesToQueuedValueCounts.update(table, previousCount + pkValues.size)
        tablesWithQueuedValues.add(table)
      }
    }
  }

  override def dequeue(): Option[DataCopyTask] = {
    this.synchronized {
      if (tablesWithQueuedValues.isEmpty) {
        None
      } else {
        // TODO would be nice to eventually make this stuff constant time
        val table: Table = tablesWithQueuedValues.maxBy(tablesToQueuedValueCounts)
        val totalQuantityAvailable: Long = tablesToQueuedValueCounts(table)
        val dequeueQuantity = Constants.dataCopyBatchSizes.filter(_ <= totalQuantityAvailable).max

        val chronicleQueueAccess: ChronicleQueueAccess = tablesToChronicleQueues(table)
        val primaryKeyValues: Seq[MultiColumnPrimaryKeyValue] = chronicleQueueAccess.read(dequeueQuantity)
        val dataCopyTask = new DataCopyTask(table, primaryKeyValues)

        tablesToQueuedValueCounts.update(table, totalQuantityAvailable - dequeueQuantity)
        if (totalQuantityAvailable == dequeueQuantity) {
          tablesWithQueuedValues.remove(table)
        }

        Some(dataCopyTask)
      }
    }
  }

  override def isEmpty(): Boolean = {
    this.synchronized {
      tablesWithQueuedValues.isEmpty
    }
  }
}
