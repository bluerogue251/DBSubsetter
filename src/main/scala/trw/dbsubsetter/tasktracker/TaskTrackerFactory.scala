package trw.dbsubsetter.tasktracker

import trw.dbsubsetter.config.Config
import trw.dbsubsetter.tasktracker.impl.{TaskTrackerImpl, TaskTrackerInstrumented}

object TaskTrackerFactory {
  def buildTaskTracker(config: Config): TaskTracker = {
    var taskTracker: TaskTracker = new TaskTrackerImpl()

    if (config.exposeMetrics) {
      taskTracker = new TaskTrackerInstrumented(taskTracker)
    }

    taskTracker
  }
}
