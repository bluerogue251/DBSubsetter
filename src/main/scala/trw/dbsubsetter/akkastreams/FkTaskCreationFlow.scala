package trw.dbsubsetter.akkastreams

import akka.NotUsed
import akka.stream.scaladsl.Flow
import trw.dbsubsetter.workflow._

object FkTaskCreationFlow {
  def flow(fkTaskCreationWorkflow: FkTaskCreationWorkflow): Flow[PksAdded, NewTasks, NotUsed] = {
    Flow[PksAdded].map(pksAdded => fkTaskCreationWorkflow.createFkTasks(pksAdded))
  }
}
