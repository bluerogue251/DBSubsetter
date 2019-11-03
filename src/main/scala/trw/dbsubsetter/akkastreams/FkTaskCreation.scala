package trw.dbsubsetter.akkastreams

import akka.NotUsed
import akka.stream.scaladsl.Flow
import trw.dbsubsetter.workflow._

private[akkastreams] object FkTaskCreation {
  def flow(fkTaskCreationWorkflow: FkTaskCreationWorkflow): Flow[PksAdded, IndexedSeq[ForeignKeyTask], NotUsed] = {
    Flow[PksAdded].map(fkTaskCreationWorkflow.createFkTasks)
  }
}
