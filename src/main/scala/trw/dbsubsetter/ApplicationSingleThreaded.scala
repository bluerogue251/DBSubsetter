package trw.dbsubsetter

import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.{DbAccessFactory, SchemaInfo}
import trw.dbsubsetter.primarykeystore.{PrimaryKeyStore, PrimaryKeyStoreFactory}
import trw.dbsubsetter.singlethreaded.{TaskTracker, TaskTrackerFactory}
import trw.dbsubsetter.workflow._

object ApplicationSingleThreaded {
  def run(config: Config, schemaInfo: SchemaInfo, baseQueries: Vector[BaseQuery]): Unit = {
    // Set up workflow objects
    val dbAccessFactory = new DbAccessFactory(config, schemaInfo)
    val originDbWorkflow = new OriginDbWorkflow(config, schemaInfo, dbAccessFactory)
    val targetDbWorkflow = new TargetDbWorkflow(config, schemaInfo, dbAccessFactory)
    val pkStore: PrimaryKeyStore = PrimaryKeyStoreFactory.getPrimaryKeyStore(schemaInfo)
    val pkWorkflow: PkStoreWorkflow = new PkStoreWorkflow(pkStore)

    // Set up task queue
    val taskTracker: TaskTracker = TaskTrackerFactory.buildTaskTracker(config)
    taskTracker.enqueueTasks(baseQueries)

    // Run task queue until empty
    while (taskTracker.nonEmpty) {
      // Get a new task to work on
      val task: OriginDbRequest = taskTracker.dequeueTask()

      // Check if we can skip this task since we've already seen this row already (if applicable)
      // TODO -- think about and write comment about why this only applies to `FetchParentTask` and not `FetchChildrenTask`
      // Maybe there _is_ a use case for checking for a `FetchChildrenTask` --> if it's a one-to-one relationship and the child table shares the same PK value
      // with the parent table, it seems in that case it might work to do a Precheck? (In most cases, the child table's value will _not_ be it's PK, so in most
      // cases the PKStore can't help us... but in the one-to-one case where the child column happens to be its PrimaryKey, it might work
      val canSkip: Boolean = task match {
        case t @ FetchParentTask(foreignKey, value) =>
          TaskPreCheck.shouldPrecheck(t) && !pkStore.alreadySeen(foreignKey.toTable, value)
        case _ => false
      }

      // Do the bulk of the actual work for the task
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
