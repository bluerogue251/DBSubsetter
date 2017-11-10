package trw.dbsubsetter.akkastreams

import akka.NotUsed
import akka.stream.scaladsl.Flow
import trw.dbsubsetter.workflow.{FkQuery, PkMissing, PkResult}

object PkMissingFlow {
  def flow: Flow[PkResult, FkQuery, NotUsed] = {
    Flow[PkResult].collect { case PkMissing(fkTask) => FkQuery(fkTask) }
  }
}
