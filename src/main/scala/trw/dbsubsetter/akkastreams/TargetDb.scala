package trw.dbsubsetter.akkastreams

import akka.NotUsed
import akka.stream.scaladsl.Flow
import trw.dbsubsetter.datacopy.DataCopyWorkflowFactory
import trw.dbsubsetter.db.DbAccessFactory
import trw.dbsubsetter.db.SchemaInfo
import trw.dbsubsetter.workflow._

private[akkastreams] object TargetDb {
  def insert(dbAccessFactory: DbAccessFactory, schemaInfo: SchemaInfo): Flow[DataCopyTask, Unit, NotUsed] = {
    val dataCopyWorkflow = DataCopyWorkflowFactory.build(dbAccessFactory, schemaInfo)
    Flow[DataCopyTask].map(dataCopyWorkflow.process)
  }
}
