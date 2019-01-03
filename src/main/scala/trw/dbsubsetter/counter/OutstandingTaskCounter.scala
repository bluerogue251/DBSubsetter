package trw.dbsubsetter.counter

trait OutstandingTaskCounter {
  def markOneTaskCompleted(): Long
  def recordNewTasksAdded(newTasks: Long): Long
}
