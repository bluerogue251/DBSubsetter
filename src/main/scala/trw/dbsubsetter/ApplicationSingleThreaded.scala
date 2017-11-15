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

      val baseQueries = BaseQueries.get(config, schemaInfo)
      val baseResults = baseQueries.map(dbWorkflow.process)
      val basePkResults = baseResults.flatMap(br => pkWorkflow.process(br))
      val newTasks: Iterable[FkTask] = basePkResults.flatMap {
        case pka: PksAdded => NewFkTaskWorkflow.process(pka, schemaInfo)
      }
      val queue = mutable.Queue.empty[FkTask]
      newTasks.foreach(t => queue.enqueue(t))

      val taskQueue = scala.collection.mutable.Queue.empty[FkTask]
      while (taskQueue.nonEmpty) {
        val next = taskQueue.dequeue()
        val nextOpt = if (next.fk.pointsToPk) pkWorkflow.process(next) else List(next)
        nextOpt.collect { case fkTask: FkTask => fkTask }.foreach { fkTask =>
          val dbResult = dbWorkflow.process(fkTask)
          val pksAdded = pkWorkflow.process(dbResult)
          val newTasks = pksAdded.collect { case pka: PksAdded => pka }.flatMap(pka => NewFkTaskWorkflow.process(pka, schemaInfo))
          newTasks.foreach(fkt => taskQueue.enqueue(fkt))
        }
      }
  }
}
