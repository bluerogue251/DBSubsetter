package trw.dbsubsetter.akkastreams

import akka.NotUsed
import akka.stream.scaladsl.Flow
import trw.dbsubsetter.workflow.{FkQuery, FkTask, PkExistRequest}

object FkTaskFlows {
  def toDbQuery: Flow[FkTask, FkQuery, NotUsed] = {
    Flow[FkTask]
      .filterNot(_.fk.pointsToPk).map(FkQuery)
  }

  def toPkStoreQuery: Flow[FkTask, PkExistRequest, NotUsed] = {
    Flow[FkTask]
      .filter(_.fk.pointsToPk).map(PkExistRequest)
  }
}
