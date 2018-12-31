package trw.dbsubsetter.akkastreams

import akka.NotUsed
import akka.stream.scaladsl.Flow
import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.SchemaInfo
import trw.dbsubsetter.util.CloseableRegistry
import trw.dbsubsetter.workflow._

object TargetDb {
  def insert(config: Config, schemaInfo: SchemaInfo, closeableRegistry: CloseableRegistry): Flow[PksAdded, TargetDbInsertResult, NotUsed] = {
    Flow[PksAdded].statefulMapConcat { () =>
      val dbWorkflow = new TargetDbWorkflow(config, schemaInfo)
      closeableRegistry.register(dbWorkflow)
      req => {
        List(dbWorkflow.process(req))
      }
    }
  }
}
