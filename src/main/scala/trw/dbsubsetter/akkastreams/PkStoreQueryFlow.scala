package trw.dbsubsetter.akkastreams

import akka.NotUsed
import akka.stream.scaladsl.Flow
import trw.dbsubsetter.db.Table
import trw.dbsubsetter.workflow._

object PkStoreQueryFlow {
  def flow(pkOrdinalsByTable: Map[Table, Seq[Int]]): Flow[PkRequest, PkResult, NotUsed] = {
    Flow[PkRequest].statefulMapConcat { () =>
      val pkStore = new PkStoreWorkflow(pkOrdinalsByTable)
      request => {
        pkStore.process(request)
      }
    }
  }
}
