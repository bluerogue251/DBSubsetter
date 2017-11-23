package trw.dbsubsetter.akkastreams

import akka.NotUsed
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.Flow
import trw.dbsubsetter.db.SchemaInfo
import trw.dbsubsetter.workflow._

object NewTasks {
  def flow(sch: SchemaInfo, numBaseQueries: Int): Flow[PkResult, FkTask, NotUsed] = {
    val f = Flow[PkResult].statefulMapConcat[(Long, Vector[FkTask])] { () =>
      var unfinishedTaskCount: Long = numBaseQueries

      // TODO: is `.size` a constant time operation? If not, consider a more suitable data structure than Vector
      pkResult => {
        pkResult match {
          case pka: PksAdded =>
            val newTasks = NewFkTaskWorkflow.process(pka, sch)
            unfinishedTaskCount += (newTasks.size - 1)
            List((unfinishedTaskCount, newTasks))
          case DuplicateTask =>
            unfinishedTaskCount -= 1
            List((unfinishedTaskCount, Vector.empty))
          case other => throw new RuntimeException(s"Cannot handle $other")
        }
      }
    }

    // Do buffer before mapConcat to make the buffer less likely to fill up
    f.takeWhile { case (count, _) => count != 0 }
      .buffer(Int.MaxValue, OverflowStrategy.fail)
      .mapConcat { case (_, tasks) => tasks }
  }
}
