package trw.dbsubsetter.fktaskqueue

import trw.dbsubsetter.db.SchemaInfo
import trw.dbsubsetter.fkcalc.ForeignKeyTask

import java.nio.file.Path

trait ForeignKeyTaskQueue {
  def enqueue(foreignKeyTask: ForeignKeyTask): Unit
  def dequeue(): Option[ForeignKeyTask]
  def nonEmpty(): Boolean
  def size(): Long
}

object ForeignKeyTaskQueue {
  def from(storageDirectory: Path, schemaInfo: SchemaInfo): ForeignKeyTaskQueue = {
    val base: ForeignKeyTaskQueue = new ForeignKeyTaskQueueImpl(storageDirectory, schemaInfo.foreignKeys)
    new ForeignKeyTaskQueueInstrumentedImpl(base)
  }
}
