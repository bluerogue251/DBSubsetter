package trw.dbsubsetter.workflow.offheap

import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.SchemaInfo
import trw.dbsubsetter.workflow.offheap.impl.OffHeapFkTaskQueueInstrumented
import trw.dbsubsetter.workflow.offheap.impl.chroniclequeue.ChronicleQueueFkTaskQueue

object OffHeapFkTaskQueueFactory {
  def buildOffHeapFkTaskQueue(config: Config, schemaInfo: SchemaInfo): OffHeapFkTaskQueue = {
    var taskQueue: OffHeapFkTaskQueue =
      new ChronicleQueueFkTaskQueue(config, schemaInfo)

    if (config.exposeMetrics) {
      taskQueue = new OffHeapFkTaskQueueInstrumented(taskQueue)
    }

    taskQueue
  }
}
