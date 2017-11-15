package trw.dbsubsetter.akkastreams

import akka.NotUsed
import akka.stream.scaladsl.Flow
import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.SchemaInfo
import trw.dbsubsetter.workflow._

object OriginDbQueryFlow {
  def flow(config: Config, schemaInfo: SchemaInfo): Flow[OriginDbRequest, OriginDbResult, NotUsed] = {
    Flow[OriginDbRequest].statefulMapConcat { () =>
      val dbWorkflow = new OriginDbWorkflow(config, schemaInfo)
      req => {
        List(dbWorkflow.process(req))
      }
    }
  }
}
