package trw.dbsubsetter

import trw.dbsubsetter.config.{CommandLineParser, Config}
import trw.dbsubsetter.db.SchemaInfoRetrieval
import trw.dbsubsetter.workflow.{BaseQueries, FkTask, OriginDbWorkflow}

import scala.collection.mutable

object ApplicationSingleThreaded extends App {
  CommandLineParser.parser.parse(args, Config()) match {
    case None => System.exit(1)
    case Some(config) =>
      val schemaInfo = SchemaInfoRetrieval.getSchemaInfo(config)
      val dbWorkflow = new OriginDbWorkflow(config, schemaInfo)
      val pkStore =

      val baseQueries = BaseQueries.get(config, schemaInfo)
      val baseResults = baseQueries.map(dbWorkflow.process)
      val queue = mutable.Queue.empty[FkTask]

      val fkTaskQueue = scala.collection.mutable.Queue.empty[FkTask]

  }
}
