package trw.dbsubsetter.akkastreams

import akka.NotUsed
import akka.stream.scaladsl.Flow
import trw.dbsubsetter.db.SchemaInfo
import trw.dbsubsetter.workflow._

object PkResultFlows {
  def pkAddedToNewTasks(sch: SchemaInfo, numBaseQueries: Int): Flow[PkResult, (Long, Vector[FkTask]), NotUsed] = {
    Flow[PkResult]
      .statefulMapConcat[(Long, Vector[FkTask])] { () =>
      var counter: Long = numBaseQueries.toLong

      pkResult => {
        pkResult match {
          case pka: PksAdded =>
            val newTasks = NewFkTaskWorkflow.process(pka, sch)
            counter += (newTasks.size - 1)
            List((counter, newTasks))
          case _ => List.empty
        }
      }
    }
  }

  def tripSwitch: Flow[(Long, Vector[FkTask]), FkTask, NotUsed] = {
    Flow[(Long, Vector[FkTask])]
      .takeWhile { case (counter, _) => counter != 0 }
      .mapConcat { case (_, newTasks) => newTasks }
  }

  def pkAddedToDbInsert(sch: SchemaInfo): Flow[PkResult, PksAdded, NotUsed] = {
    Flow[PkResult].collect { case pka: PksAdded => pka }
  }

  def pkMissingToFkQuery: Flow[PkResult, FkTask, NotUsed] = {
    Flow[PkResult].collect { case fkTask: FkTask => fkTask }
  }
}
