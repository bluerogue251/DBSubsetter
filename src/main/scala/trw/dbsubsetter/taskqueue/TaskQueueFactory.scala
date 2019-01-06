package trw.dbsubsetter.taskqueue

import trw.dbsubsetter.config.Config
import trw.dbsubsetter.taskqueue.impl.{TaskQueueImpl, TaskQueueInstrumented}

object TaskQueueFactory {
  def buildTaskTracker(config: Config): TaskQueue = {
    var taskTracker: TaskQueue = new TaskQueueImpl()

    if (config.exposeMetrics) {
      taskTracker = new TaskQueueInstrumented(taskTracker)
    }

    taskTracker
  }
}
