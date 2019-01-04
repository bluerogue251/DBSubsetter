package trw.dbsubsetter

import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.{DbAccessFactory, SchemaInfo}
import trw.dbsubsetter.singlethreaded.{TaskTracker, TaskTrackerFactory}
import trw.dbsubsetter.workflow._

object ApplicationSingleThreaded {
  def run(config: Config, schemaInfo: SchemaInfo, baseQueries: Vector[BaseQuery]): Unit = {
    // Set up workflow objects
    val dbAccessFactory = new DbAccessFactory(config, schemaInfo)
    val originDbWorkflow = new OriginDbWorkflow(config, schemaInfo, dbAccessFactory)
    val targetDbWorkflow = new TargetDbWorkflow(config, schemaInfo, dbAccessFactory)
    val pkWorkflow = new PkStoreWorkflow(schemaInfo)

    // Set up task queue
    val taskTracker: TaskTracker = TaskTrackerFactory.buildTaskTracker(config)
    taskTracker.enqueueTasks(baseQueries)

    // Run task queue until empty
    while (taskTracker.nonEmpty) {
      // Get a new task to work on
      val task: OriginDbRequest = taskTracker.dequeueTask()

      // Check if we can skip this task since we've already seen this row already (if applicable)
      // TODO -- think about and write comment about why this only applies to `FetchParentTask` and not `FetchChildrenTask`
      val canSkip: Boolean = task match {
        case fetchParentTask @ FetchParentTask(_, _) =>
          TaskPreCheck.shouldPrecheck(fetchParentTask) &&
            pkWorkflow.exists(fetchParentTask) != DuplicateTask
        case _ => false
      }

      if (!canSkip) {
        val dbResult = originDbWorkflow.process(task)
        val pksAdded = if (dbResult.table.storePks) pkWorkflow.add(dbResult) else SkipPkStore.process(dbResult)
        targetDbWorkflow.process(pksAdded)
        val newTasks = NewFkTaskWorkflow.process(pksAdded, schemaInfo)
        newTasks.foreach { case ((foreignKey, fetchChildren), fkValues) =>
          val tasks = if (fetchChildren) {
            fkValues.map(value => FetchChildrenTask(foreignKey, value))
          } else {
            fkValues.map(value => FetchParentTask(foreignKey, value))
          }
          taskTracker.enqueueTasks(tasks)
        }
      }

    }

    // Ensure all SQL connections get closed
    dbAccessFactory.closeAllConnections()
  }
}
