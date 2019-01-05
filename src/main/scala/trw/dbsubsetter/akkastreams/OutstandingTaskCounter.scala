package trw.dbsubsetter.akkastreams

import akka.NotUsed
import akka.stream.scaladsl.Flow
import trw.dbsubsetter.workflow.NewTasks

// TODO try to make the Array[Any] type more specific
object OutstandingTaskCounter {
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
