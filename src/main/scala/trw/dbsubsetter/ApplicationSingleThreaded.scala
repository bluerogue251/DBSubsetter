package trw.dbsubsetter

import trw.dbsubsetter.config.{CommandLineParser, Config}
import trw.dbsubsetter.db.SchemaInfoRetrieval
import trw.dbsubsetter.workflow._

import scala.collection.mutable

object ApplicationSingleThreaded extends App {
  CommandLineParser.parser.parse(args, Config()) match {
    case None => System.exit(1)
    case Some(config) =>
      val schemaInfo = SchemaInfoRetrieval.getSchemaInfo(config)
      val originDbWorkflow = new OriginDbWorkflow(config, schemaInfo)
      val targetDbWorkflow = new TargetDbWorkflow(config, schemaInfo)

      val pkWorkflow = new PkStoreWorkflow(schemaInfo.pkOrdinalsByTable)
      val queue = mutable.Queue.empty[OriginDbRequest]
      BaseQueries.get(config, schemaInfo).foreach(t => queue.enqueue(t))

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
  }
}
