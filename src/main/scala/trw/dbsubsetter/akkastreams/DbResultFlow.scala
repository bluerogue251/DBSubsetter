package trw.dbsubsetter.akkastreams

import akka.NotUsed
import akka.stream.scaladsl.Flow
import trw.dbsubsetter.workflow._

object DbResultFlow {
  def toPkAddRequest: Flow[DbResult, PkAddRequest, NotUsed] = {
    Flow[DbResult]
      .collect { case DbFetchResult(table, rows, fetchChildren) => PkAddRequest(table, rows, fetchChildren) }
  }

  def toDbCopyResult: Flow[DbResult, DbCopyResult, NotUsed] = {
    Flow[DbResult]
      .collect { case r: DbCopyResult => r }
  }
}
