package trw.dbsubsetter.akkastreams

import akka.NotUsed
import akka.stream.scaladsl.Flow
import trw.dbsubsetter.workflow.FkTask

object FkTaskFlows {
  def toDbQuery: Flow[FkTask, FkTask, NotUsed] = {
    Flow[FkTask]
      .filterNot(_.fk.pointsToPk)
  }

  def toPkStoreQuery: Flow[FkTask, FkTask, NotUsed] = {
    Flow[FkTask]
      .filter(_.fk.pointsToPk)
  }
}
