package trw.dbsubsetter.akkastreams

import akka.NotUsed
import akka.stream.scaladsl.Flow
import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.SchemaInfo
import trw.dbsubsetter.util.CloseableRegistry
import trw.dbsubsetter.workflow._

object OriginDb {
  def query(config: Config, schemaInfo: SchemaInfo, closeableRegistry: CloseableRegistry): Flow[OriginDbRequest, OriginDbResult, NotUsed] = {
    Flow[OriginDbRequest].statefulMapConcat { () =>
      val dbWorkflow = new OriginDbWorkflow(config, schemaInfo)
      closeableRegistry.register(dbWorkflow)
      req => {
        List(dbWorkflow.process(req))
      }
    }
  }
}
