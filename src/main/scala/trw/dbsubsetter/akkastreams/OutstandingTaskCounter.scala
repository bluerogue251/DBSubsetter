package trw.dbsubsetter.akkastreams

import akka.NotUsed
import akka.stream.scaladsl.Flow
import trw.dbsubsetter.workflow.NewTasks

// TODO consider whether this has anything to do with it working previously with the FkTaskBuffer...
//   even without the custom onUpstreamFinish thing going on...
//   Or, try to figure out what this is for.
private[akkastreams] object OutstandingTaskCounter {
  def counter(numBaseQueries: Int): Flow[NewTasks, NewTasks, NotUsed] = {
    val counterFlow = Flow[NewTasks].statefulMapConcat { () =>
      var statefulCounter: Long = numBaseQueries

      newTasks => {
        statefulCounter -= 1
        newTasks.taskInfo.foreach { case ((_, _), fkValues) =>
          statefulCounter += fkValues.length
        }
        List((statefulCounter, newTasks))
      }
    }

    val circuitBreaker = Flow[(Long, NewTasks)].takeWhile { case (counter, _) => counter != 0 }

    val simplifier = Flow[(Long, NewTasks)].map { case (_, newTasks) => newTasks }

    counterFlow.via(circuitBreaker).via(simplifier)
  }
}
