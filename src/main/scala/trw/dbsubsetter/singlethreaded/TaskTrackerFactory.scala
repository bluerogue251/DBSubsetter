package trw.dbsubsetter.singlethreaded

import trw.dbsubsetter.config.Config
import trw.dbsubsetter.singlethreaded.impl.{TaskTrackerImpl, TaskTrackerInstrumented}

object TaskTrackerFactory {
  def buildTaskTracker(config: Config): TaskTracker = {
    var taskTracker: TaskTracker = new TaskTrackerImpl()

    if (config.exposeMetrics) {
      taskTracker = new TaskTrackerInstrumented(taskTracker)
    }

    taskTracker
  }
}
