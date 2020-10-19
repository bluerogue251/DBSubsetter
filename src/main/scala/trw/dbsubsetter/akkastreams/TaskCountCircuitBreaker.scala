package trw.dbsubsetter.akkastreams

import akka.NotUsed
import akka.stream.scaladsl.Flow
import trw.dbsubsetter.workflow.ForeignKeyTask

/**
  * Keeps track of the count of currently outstanding tasks and completes when that count reaches zero.
  * This allows stream processing to eventually complete even though the graph contains cycles.
  */
private[akkastreams] object TaskCountCircuitBreaker {
  def statefulCounter(initialCount: Long): Flow[IndexedSeq[ForeignKeyTask], IndexedSeq[ForeignKeyTask], NotUsed] = {
    var count: Long = initialCount

    val counterFlow: Flow[IndexedSeq[ForeignKeyTask], (Long, IndexedSeq[ForeignKeyTask]), NotUsed] =
      Flow[IndexedSeq[ForeignKeyTask]].map { newTasks =>
        count -= 1 // Exactly one previous task was completed in creating this new batch of tasks
        count += newTasks.length
        (count, newTasks)
      }

    val circuitBreaker = Flow[(Long, IndexedSeq[ForeignKeyTask])].takeWhile { case (counter, _) => counter != 0 }

    val simplifier = Flow[(Long, IndexedSeq[ForeignKeyTask])].map { case (_, newTasks) => newTasks }

    counterFlow.via(circuitBreaker).via(simplifier)
  }
}
