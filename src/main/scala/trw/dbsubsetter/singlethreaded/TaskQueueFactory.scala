package trw.dbsubsetter.singlethreaded

import trw.dbsubsetter.config.Config
import trw.dbsubsetter.singlethreaded.impl.{TaskTrackerImpl, TaskTrackerInstrumented}

object TaskQueueFactory {
  def buildTaskQueue(config: Config): TaskTracker = {
    var taskQueue: TaskTracker = new TaskTrackerImpl()

    if (config.exposeMetrics) {
      taskQueue = new TaskTrackerInstrumented(taskQueue)
    }

    taskQueue
  }
}
