package trw.dbsubsetter.akkastreams

import akka.NotUsed
import akka.stream.scaladsl.Flow
import trw.dbsubsetter.db.DbAccessFactory
import trw.dbsubsetter.workflow._

private[akkastreams] object OriginDb {

  def query(
      dbAccessFactory: DbAccessFactory
  ): Flow[ForeignKeyTask, OriginDbResult, NotUsed] = {
    Flow[ForeignKeyTask].statefulMapConcat { () =>
      val dbWorkflow = new ForeignKeyTaskHandler(dbAccessFactory)
      req => {
        List(dbWorkflow.handle(req))
      }
    }
  }
}
