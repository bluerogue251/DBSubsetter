package trw.dbsubsetter.akkastreams

import akka.NotUsed
import akka.stream.scaladsl.Flow
import trw.dbsubsetter.workflow._

object NewTasks {
  def flow(newFkTasksWorkflow: NewFkTasksWorkflow): Flow[PksAdded, NewTasks, NotUsed] = {
    Flow[PksAdded].map(pksAdded => newFkTasksWorkflow.process(pksAdded))
  }
}
