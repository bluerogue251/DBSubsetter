package trw.dbsubsetter.akkastreams

import akka.NotUsed
import akka.stream.scaladsl.Flow
import trw.dbsubsetter.db.ForeignKey

object OutstandingTaskCounter {
  def counter(numBaseQueries: Int): Flow[Map[(ForeignKey, Boolean), Vector[Any]], Map[(ForeignKey, Boolean), Vector[Any]], NotUsed] = {
    val counterFlow = Flow[Map[(ForeignKey, Boolean), Vector[Any]]].statefulMapConcat { () =>
      var statefulCounter: Long = numBaseQueries

      incoming => {
        statefulCounter -= 1
        incoming.foreach { case ((_, _), fkValues) =>
          statefulCounter += fkValues.size
        }

        List((statefulCounter, incoming))
      }
    }

    val circuitBreaker = Flow[(Long, Map[(ForeignKey, Boolean), Vector[Any]])].takeWhile { case ((counter, _)) => counter != 0 }

    val simplifier = Flow[(Long, Map[(ForeignKey, Boolean), Vector[Any]])].map { case ((_, newTasks)) => newTasks }

    counterFlow.via(circuitBreaker).via(simplifier)
  }
}
