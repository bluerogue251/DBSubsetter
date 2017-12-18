package trw.dbsubsetter.akkastreams

import akka.actor.{Actor, Props}
import trw.dbsubsetter.db.SchemaInfo
import trw.dbsubsetter.workflow.{FkTask, OriginDbResult, PkStoreWorkflow}

class PkStore(sch: SchemaInfo) extends Actor {
  val pkStoreWorkflow = new PkStoreWorkflow(sch)

  override def receive: Receive = {
    case req: FkTask => sender() ! pkStoreWorkflow.exists(req)
    case req: OriginDbResult => sender() ! pkStoreWorkflow.add(req)
    case other => throw new RuntimeException(s"Cannot handle $other")
  }
}

object PkStore {
  def props(sch: SchemaInfo): Props = {
    Props(new PkStore(sch))
  }
}