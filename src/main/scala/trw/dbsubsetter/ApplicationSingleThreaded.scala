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
      val dbWorkflow = new OriginDbWorkflow(config, schemaInfo)
      val pkWorkflow = new PkStoreWorkflow(schemaInfo)
      val queue = mutable.Queue.empty[OriginDbRequest]

      val baseQueries = BaseQueries.get(config, schemaInfo)
      baseQueries.foreach(t => queue.enqueue(t))

      while (queue.nonEmpty) {
        val taskOpt: List[OriginDbRequest] = queue.dequeue() match {
          case t: FkTask if t.fk.pointsToPk => pkWorkflow.process(t).collect { case t: FkTask => t }
          case t: OriginDbRequest => List(t)
        }
        taskOpt.foreach { task =>
          val dbResult = dbWorkflow.process(task)
          val pksAdded = pkWorkflow.process(dbResult)
          val newTasks = pksAdded.collect { case pka: PksAdded => pka }.flatMap(pka => NewFkTaskWorkflow.process(pka, schemaInfo))
          newTasks.foreach(fkt => queue.enqueue(fkt))
        }
      }
  }
}
