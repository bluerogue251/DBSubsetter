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
    val pkStore: PrimaryKeyStore = PrimaryKeyStoreFactory.buildPrimaryKeyStore(schemaInfo)
    val pkWorkflow: PkStoreWorkflow = new PkStoreWorkflow(pkStore, schemaInfo)

    // Set up task queue
    val taskTracker: TaskTracker = TaskTrackerFactory.buildTaskTracker(config)
    taskTracker.enqueueTasks(baseQueries)

    // Run task queue until empty
    while (taskTracker.nonEmpty) {
      val taskOpt: List[OriginDbRequest] = taskTracker.dequeueTask() match {
        case t: FkTask if FkTaskPreCheck.shouldPrecheck(t) => List(pkStore.alreadySeen(t)).collect { case t: FkTask => t }
        case t => List(t)
      }
      taskOpt.foreach { task =>
        val dbResult = originDbWorkflow.process(task)
        val pksAdded = if (dbResult.table.storePks) pkWorkflow.add(dbResult) else SkipPkStore.process(dbResult)
        targetDbWorkflow.process(pksAdded)
        val newTasks = NewFkTaskWorkflow.process(pksAdded, schemaInfo)
        newTasks.foreach { case ((fk, fetchChildren), fkValues) =>
          val tasks = fkValues.map { v =>
            val table = if (fetchChildren) fk.fromTable else fk.toTable
            FkTask(table, fk, v, fetchChildren)
          }
          taskTracker.enqueueTasks(tasks)
        }
      }
    }

    // Ensure all SQL connections get closed
    dbAccessFactory.closeAllConnections()
  }
}
