package trw.dbsubsetter

import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.SchemaInfo
import trw.dbsubsetter.workflow._

import scala.collection.mutable

object ApplicationSingleThreaded {
  def run(config: Config, schemaInfo: SchemaInfo, baseQueries: Iterable[SqlStrQuery]): Unit = {
    // Set up workflow objects
    val originDbWorkflow = new OriginDbWorkflow(config, schemaInfo)
    val targetDbWorkflow = new TargetDbWorkflow(config, schemaInfo)
    val pkWorkflow = new PkStoreWorkflow(schemaInfo.pkOrdinalsByTable)

    // Set up task queue
    val queue = mutable.Queue.empty[OriginDbRequest]
    baseQueries.foreach(t => queue.enqueue(t))

    // Run task queue until empty
    while (queue.nonEmpty) {
      val taskOpt: List[OriginDbRequest] = queue.dequeue() match {
        case t: FkTask if FkTaskPreCheck.canPrecheck(t) => List(pkWorkflow.exists(t)).collect { case t: FkTask => t }
        case t => List(t)
      }
      taskOpt.foreach { task =>
        val dbResult = originDbWorkflow.process(task)
        val pksAdded = pkWorkflow.add(dbResult)
        targetDbWorkflow.process(pksAdded)
        val newTasks = NewFkTaskWorkflow.process(pksAdded, schemaInfo)
        newTasks.values.foreach { tasks =>
          queue.enqueue(tasks: _*)
        }
      }
    }
  }
}
