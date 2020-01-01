package trw.dbsubsetter.akkastreams

import akka.NotUsed
import akka.stream.scaladsl.Flow
import trw.dbsubsetter.datacopy.impl.GenericDataCopyWorkflowImpl
import trw.dbsubsetter.db.DbAccessFactory
import trw.dbsubsetter.workflow._

private[akkastreams] object TargetDb {
  def insert(dbAccessFactory: DbAccessFactory): Flow[DataCopyTask, Unit, NotUsed] = {
    val dbWorkflow = new GenericDataCopyWorkflowImpl(dbAccessFactory)
    Flow[DataCopyTask].map(dbWorkflow.process)
  }
}
