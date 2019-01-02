package trw.dbsubsetter.akkastreams

import akka.NotUsed
import akka.stream.scaladsl.Flow
import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.{DbAccessFactory, SchemaInfo}
import trw.dbsubsetter.workflow._

object OriginDb {
  def query(config: Config, schemaInfo: SchemaInfo, dbAccessFactory: DbAccessFactory): Flow[OriginDbRequest, OriginDbResult, NotUsed] = {
    Flow[OriginDbRequest].statefulMapConcat { () =>
      val dbWorkflow = new OriginDbWorkflow(config, schemaInfo, dbAccessFactory)
      req => {
        List(dbWorkflow.process(req))
      }
    }
  }
}
