package trw.dbsubsetter.akkastreams

import akka.actor.Actor
import trw.dbsubsetter.db.Table
import trw.dbsubsetter.workflow.{PkRequest, PkStoreWorkflow}

class PkStore(pkOrdinalsByTable: Map[Table, Seq[Int]]) extends Actor {
  val pkStoreWorkflow = new PkStoreWorkflow(pkOrdinalsByTable)

  override def receive: Receive = {
    case req: PkRequest => sender() ! pkStoreWorkflow.process(req)
  }
}
