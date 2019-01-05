package trw.dbsubsetter.akkastreams

import akka.NotUsed
import akka.stream.scaladsl.Flow
import trw.dbsubsetter.db.SchemaInfo
import trw.dbsubsetter.workflow._

object NewTasks {
  def flow(sch: SchemaInfo): Flow[PksAdded, NewTasks, NotUsed] = {
    Flow[PksAdded].map(pksAdded => NewFkTaskWorkflow.process(pksAdded, sch))
  }
}
