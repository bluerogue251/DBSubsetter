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

    // Buffer comes before mapConcat so that the buffer is less likely to fill up
    // OverflowStrategy.fail may help avoid deadlocks given that this is a feedback loop.
    // But I haven't fully thought through whether there is a better way using backpressure or not
    // See https://doc.akka.io/docs/akka/2.5.6/scala/stream/stream-graphs.html#graph-cycles-liveness-and-deadlocks
    f.takeWhile { case (count, _) => count != 0 }
      .buffer(Int.MaxValue, OverflowStrategy.fail)
      .mapConcat { case (_, tasks) => tasks }
  }
}
