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

    val start = System.nanoTime()
    // Run task queue until empty
    while (queue.nonEmpty) {
      val taskOpt: List[OriginDbRequest] = queue.dequeue() match {
        case t: FkTask if FkTaskPreCheck.canBePrechecked(t) => pkWorkflow.process(t).collect { case t: FkTask => t }
        case t => List(t)
      }
      taskOpt.foreach { task =>
        val dbResult = originDbWorkflow.process(task)
        val pksAdded = pkWorkflow.process(dbResult).collect { case pka: PksAdded => pka }
        pksAdded.foreach(targetDbWorkflow.process)
        val newTasks = pksAdded.flatMap(pka => NewFkTaskWorkflow.process(pka, schemaInfo))
        newTasks.foreach(fkt => queue.enqueue(fkt))
      }
    }
    val end = System.nanoTime()
    val tookSeconds = (end - start) / 1000000000
    println(s"Done! Took $tookSeconds seconds")
  }
}
