package trw.dbsubsetter.akkastreams

import akka.NotUsed
import akka.stream.scaladsl.Flow
import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.{DbAccessFactory, SchemaInfo}
import trw.dbsubsetter.workflow._

private[akkastreams] object TargetDb {
  def insert(config: Config, schemaInfo: SchemaInfo, dbAccessFactory: DbAccessFactory): Flow[PksAdded, TargetDbInsertResult, NotUsed] = {
    Flow[PksAdded].statefulMapConcat { () =>
      val dbWorkflow = new TargetDbWorkflow(config, schemaInfo, dbAccessFactory)
      req => {
        List(dbWorkflow.process(req))
      }
    }
  }
}
