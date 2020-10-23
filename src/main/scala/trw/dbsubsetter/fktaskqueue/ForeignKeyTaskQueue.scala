package trw.dbsubsetter.fktaskqueue

import java.nio.file.Path

import trw.dbsubsetter.db.SchemaInfo
import trw.dbsubsetter.fkcalc.ForeignKeyTask

trait ForeignKeyTaskQueue {
  def enqueue(foreignKeyTask: ForeignKeyTask): Unit
  def dequeue(): Option[ForeignKeyTask]
  def isEmpty(): Boolean
  def nonEmpty(): Boolean
  def size(): Long
}

object ForeignKeyTaskQueue {
  def from(storageDirectory: Path, schemaInfo: SchemaInfo): ForeignKeyTaskQueue = {
    val base: ForeignKeyTaskQueue = new ForeignKeyTaskChronicleQueue(storageDirectory, schemaInfo)
    new ForeignKeyTaskQueueInstrumented(base)
  }
}
