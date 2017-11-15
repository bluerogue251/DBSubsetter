package trw.dbsubsetter.akkastreams

import akka.NotUsed
import akka.stream.scaladsl.Flow
import trw.dbsubsetter.db.SchemaInfo
import trw.dbsubsetter.workflow._

object PkStoreQueryFlow {
  def flow(schemaInfo: SchemaInfo): Flow[PkRequest, PkResult, NotUsed] = {
    Flow[PkRequest].statefulMapConcat { () =>
      val pkStore = new PkStoreWorkflow(schemaInfo)
      request => {
        pkStore.process(request)
      }
    }
  }
}
