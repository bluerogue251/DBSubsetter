package trw.dbsubsetter.akkastreams

import akka.Done
import akka.stream.scaladsl.Sink
import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.{DbAccessFactory, SchemaInfo}
import trw.dbsubsetter.workflow._

import scala.concurrent.Future

private[akkastreams] object TargetDb {
  def insert(config: Config, schemaInfo: SchemaInfo, dbAccessFactory: DbAccessFactory): Sink[PksAdded, Future[Done]] = {
    val dbWorkflow = new TargetDbWorkflow(config, schemaInfo, dbAccessFactory)
    Sink.foreach[PksAdded](dbWorkflow.process)
  }
}
