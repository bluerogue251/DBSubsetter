package trw.dbsubsetter.akkastreams

import akka.NotUsed
import akka.stream.scaladsl.Flow
import trw.dbsubsetter.workflow._

object OriginDbResultFlows {
  def toPkAddRequest: Flow[OriginDbResult, PkAddRequest, NotUsed] = {
    Flow[OriginDbResult]
      .collect { case OriginDbResult(table, rows, fetchChildren) => PkAddRequest(table, rows, fetchChildren) }
  }
}
