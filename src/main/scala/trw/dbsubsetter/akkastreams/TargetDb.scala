package trw.dbsubsetter.akkastreams

import akka.NotUsed
import akka.stream.scaladsl.Flow
import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.{DbAccessFactory, SchemaInfo}
import trw.dbsubsetter.workflow._

private[akkastreams] object TargetDb {
  def insert(config: Config, schemaInfo: SchemaInfo, dbAccessFactory: DbAccessFactory): Flow[PksAdded, Unit, NotUsed] = {
    val dbWorkflow = new TargetDbWorkflow(config, schemaInfo, dbAccessFactory)
    Flow[PksAdded].map(dbWorkflow.process)
  }
}
