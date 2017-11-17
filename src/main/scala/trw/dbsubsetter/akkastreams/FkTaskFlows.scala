package trw.dbsubsetter.akkastreams

import akka.NotUsed
import akka.stream.scaladsl.Flow
import trw.dbsubsetter.workflow.{FkTask, FkTaskPreCheck}

object FkTaskFlows {
  def toDbQuery: Flow[FkTask, FkTask, NotUsed] = {
    Flow[FkTask]
      .filterNot(FkTaskPreCheck.canBePrechecked)
  }

  def toPkStoreQuery: Flow[FkTask, FkTask, NotUsed] = {
    Flow[FkTask]
      .filter(FkTaskPreCheck.canBePrechecked)
  }
}
