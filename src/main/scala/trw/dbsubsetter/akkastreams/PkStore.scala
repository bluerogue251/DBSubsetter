package trw.dbsubsetter.akkastreams

import akka.actor.{Actor, Props}
import trw.dbsubsetter.db.Table
import trw.dbsubsetter.workflow.{FkTask, OriginDbResult, PkStoreWorkflow}

class PkStore(pkOrdinalsByTable: Map[Table, Seq[Int]]) extends Actor {
  val pkStoreWorkflow = new PkStoreWorkflow(pkOrdinalsByTable)

  override def receive: Receive = {
    case req: FkTask => sender() ! pkStoreWorkflow.exists(req)
    case req: OriginDbResult => sender() ! pkStoreWorkflow.add(req)
    case other => throw new RuntimeException(s"Cannot handle $other")
  }
}

object PkStore {
  def props(pkOrdinalsByTable: Map[Table, Seq[Int]]): Props = {
    Props(new PkStore(pkOrdinalsByTable))
  }
}