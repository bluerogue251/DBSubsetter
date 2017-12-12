package trw.dbsubsetter.akkastreams

import akka.NotUsed
import akka.stream.scaladsl.Flow
import trw.dbsubsetter.db.SchemaInfo
import trw.dbsubsetter.workflow._

object NewTasks {
  def flow(sch: SchemaInfo, numBaseQueries: Int): Flow[PkResult, Unit, NotUsed] = {
    Flow[PkResult].statefulMapConcat[Unit] { () =>
      var unfinishedTaskCount: Long = numBaseQueries

      pkResult => {
        pkResult match {
          case pka: PksAdded =>
            val newTasks = NewFkTaskWorkflow.process(pka, sch)
            unfinishedTaskCount += (newTasks.size - 1)
            newTasks.foreach { task =>
              ???
            }
          case DuplicateTask =>
            unfinishedTaskCount -= 1
          case other =>
            throw new RuntimeException(s"Cannot handle $other") // TODO: Make this a compile time error
        }

        if (unfinishedTaskCount == 0) {
          // Write to the queue that we are finished
          ???
        }

        Nil
      }
    }
  }
}
