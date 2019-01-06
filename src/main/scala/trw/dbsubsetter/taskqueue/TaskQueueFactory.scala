package trw.dbsubsetter.taskqueue

import trw.dbsubsetter.config.Config
import trw.dbsubsetter.taskqueue.impl.{TaskQueueImpl, TaskQueueInstrumented}

object TaskQueueFactory {

  def buildInMemoryTaskQueue(config: Config): TaskQueue = {
    var taskQueue: TaskQueue = new TaskQueueImpl()

    if (config.exposeMetrics) {
      taskQueue = new TaskQueueInstrumented(taskQueue)
    }

    taskQueue
  }

  def buildChronicleQueueTaskQueue(config: Config): TaskQueue = {
    var taskQueue: TaskQueue = new
  }
}
