package trw.dbsubsetter

import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.{DbAccessFactory, SchemaInfo}
import trw.dbsubsetter.primarykeystore.{PrimaryKeyStore, PrimaryKeyStoreFactory}
import trw.dbsubsetter.taskqueue.{TaskQueue, TaskQueueFactory}
import trw.dbsubsetter.workflow._

object ApplicationSingleThreaded {
  def run(config: Config, schemaInfo: SchemaInfo, baseQueries: Vector[BaseQuery]): Unit = {
    // Set up workflow objects
    val dbAccessFactory = new DbAccessFactory(config, schemaInfo)
    val originDbWorkflow = new OriginDbWorkflow(config, schemaInfo, dbAccessFactory)
    val targetDbWorkflow = new TargetDbWorkflow(config, schemaInfo, dbAccessFactory)
    val pkStore: PrimaryKeyStore = PrimaryKeyStoreFactory.buildPrimaryKeyStore(schemaInfo)
    val pkWorkflow: PkStoreWorkflow = new PkStoreWorkflow(pkStore, schemaInfo)
    val fkTaskCreationWorkflow: FkTaskCreationWorkflow = new FkTaskCreationWorkflow(schemaInfo)

    // Set up task queue
    val taskQueue: TaskQueue = TaskQueueFactory.buildTaskQueue(config)
    taskQueue.enqueueTasks(baseQueries)

    // Run task queue until empty
    while (taskQueue.nonEmpty) {
      val taskOpt: Option[OriginDbRequest] = taskQueue.dequeueTask() match {
        case t: FkTask if FkTaskPreCheck.shouldPrecheck(t) =>
          if (pkStore.alreadySeen(t.table, t.fkValue)) None else Some(t)
        case t =>
          Some(t)
      }
      taskOpt.foreach { task =>
        val dbResult = originDbWorkflow.process(task)
        val pksAdded = if (dbResult.table.storePks) pkWorkflow.add(dbResult) else SkipPkStore.process(dbResult)
        targetDbWorkflow.process(pksAdded)
        val newTasks = fkTaskCreationWorkflow.createFkTasks(pksAdded)
        newTasks.taskInfo.foreach { case ((fk, fetchChildren), fkValues) =>
          val tasks = fkValues.map { v =>
            val table = if (fetchChildren) fk.fromTable else fk.toTable
            FkTask(table, fk, v, fetchChildren)
          }
          taskQueue.enqueueTasks(tasks)
        }
      }
    }

    // Ensure all SQL connections get closed
    dbAccessFactory.closeAllConnections()
  }
}
