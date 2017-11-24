package trw.dbsubsetter.akkastreams

import akka.NotUsed
import akka.stream.scaladsl.Flow
import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.SchemaInfo
import trw.dbsubsetter.workflow._

object TargetDb {
  def insert(config: Config, schemaInfo: SchemaInfo): Flow[PksAdded, TargetDbInsertResult, NotUsed] = {
    Flow[PksAdded].statefulMapConcat { () =>
      val db = new TargetDbWorkflow(config, schemaInfo)
      req => {
        List(db.process(req))
      }
    }
  }
}
