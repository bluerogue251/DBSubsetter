package trw.dbsubsetter.taskqueue

import trw.dbsubsetter.config.Config
import trw.dbsubsetter.taskqueue.impl.{InMemoryTaskQueueImpl, TaskQueueInstrumented}

object TaskQueueFactory {

  def buildTaskQueue(config: Config): TaskQueue = {
    var taskQueue: TaskQueue = new InMemoryTaskQueueImpl()

    if (config.exposeMetrics) {
      taskQueue = new TaskQueueInstrumented(taskQueue)
    }

    taskQueue
  }
}
