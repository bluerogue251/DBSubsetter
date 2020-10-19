package trw.dbsubsetter.akkastreams

import akka.NotUsed
import akka.stream.scaladsl.Flow
import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.{DbAccessFactory, SchemaInfo}
import trw.dbsubsetter.workflow._

private[akkastreams] object OriginDb {

  def query(
      config: Config,
      schemaInfo: SchemaInfo,
      dbAccessFactory: DbAccessFactory
  ): Flow[ForeignKeyTask, OriginDbResult, NotUsed] = {
    Flow[ForeignKeyTask].statefulMapConcat { () =>
      val dbWorkflow = new OriginDbWorkflow(dbAccessFactory)
      req => {
        List(dbWorkflow.process(req))
      }
    }
  }
}
