package trw.dbsubsetter.akkastreams

import akka.NotUsed
import akka.stream.scaladsl.Flow
import trw.dbsubsetter.db.{ForeignKey, SchemaInfo}
import trw.dbsubsetter.workflow._

object NewTasks {
  def flow(sch: SchemaInfo): Flow[PkResult, Map[(ForeignKey, Boolean), Array[Any]], NotUsed] = {
    Flow[PkResult].map[Map[(ForeignKey, Boolean), Array[Any]]] { pkResult => {
      pkResult match {
        case pka: PksAdded =>
          NewFkTaskWorkflow.process(pka, sch)
        case DuplicateTask => // TODO should go to the counter instead of coming here first
          Map.empty
        case other =>
          throw new RuntimeException(s"Cannot handle $other") // TODO: Make this a compile time error
      }
    }
    }
  }
}
