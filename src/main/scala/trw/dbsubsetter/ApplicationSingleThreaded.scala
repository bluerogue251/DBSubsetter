package trw.dbsubsetter

import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.{DbAccessFactory, SchemaInfo}
import trw.dbsubsetter.workflow._

import scala.collection.mutable

object ApplicationSingleThreaded {
  def run(config: Config, schemaInfo: SchemaInfo, baseQueries: Iterable[SqlStrQuery]): Unit = {
    // Set up workflow objects
    val dbAccessFactory = new DbAccessFactory(config, schemaInfo)
    val originDbWorkflow = new OriginDbWorkflow(config, schemaInfo, dbAccessFactory)
    val targetDbWorkflow = new TargetDbWorkflow(config, schemaInfo, dbAccessFactory)
    val pkWorkflow = new PkStoreWorkflow(schemaInfo)

    // Set up task queue
    val queue = mutable.Queue.empty[OriginDbRequest]
    baseQueries.foreach(t => queue.enqueue(t))

    // Run task queue until empty
    while (queue.nonEmpty) {
      val taskOpt: List[OriginDbRequest] = queue.dequeue() match {
        case t: FkTask if FkTaskPreCheck.shouldPrecheck(t) => List(pkWorkflow.exists(t)).collect { case t: FkTask => t }
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
          queue.enqueue(tasks: _*)
        }
      }
    }

    // Ensure all SQL connections get closed
    dbAccessFactory.closeAllConnections()
  }
}
