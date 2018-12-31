package trw.dbsubsetter.akkastreams

import akka.NotUsed
import akka.stream.scaladsl.Flow
import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.{ConnectionFactory, SchemaInfo}
import trw.dbsubsetter.workflow._

object TargetDb {
  def insert(config: Config, schemaInfo: SchemaInfo, connectionFactory: ConnectionFactory): Flow[PksAdded, TargetDbInsertResult, NotUsed] = {
    Flow[PksAdded].statefulMapConcat { () =>
      val dbWorkflow = new TargetDbWorkflow(config, schemaInfo, connectionFactory)
      req => {
        List(dbWorkflow.process(req))
      }
    }
  }
}
