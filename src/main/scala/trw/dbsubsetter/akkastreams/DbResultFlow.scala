package trw.dbsubsetter.akkastreams

import akka.NotUsed
import akka.stream.scaladsl.Flow
import trw.dbsubsetter.workflow._

object DbResultFlow {
  def flow: Flow[DbResult, PkAddRequest, NotUsed] = {
    Flow[DbResult]
      .collect { case DbResult(table, rows, fetchChildren) => PkAddRequest(table, rows, fetchChildren) }
  }
}
