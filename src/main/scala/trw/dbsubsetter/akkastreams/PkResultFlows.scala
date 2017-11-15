package trw.dbsubsetter.akkastreams

import akka.NotUsed
import akka.stream.scaladsl.Flow
import trw.dbsubsetter.db.SchemaInfo
import trw.dbsubsetter.workflow._

object PkResultFlows {
  def pkAddedToNewTasks(sch: SchemaInfo): Flow[PkResult, FkTask, NotUsed] = {
    Flow[PkResult]
      .mapConcat {
        case pka: PksAdded => NewFkTaskWorkflow.process(pka, sch)
        case _ => List.empty
      }
  }

  def pkAddedToDbInsert(sch: SchemaInfo): Flow[PkResult, TargetDbInsertRequest, NotUsed] = {
    Flow[PkResult]
      .collect { case PksAdded(table, rows, _) => TargetDbInsertRequest(table, rows) }
  }

  def pkMissingToFkQuery: Flow[PkResult, FkTask, NotUsed] = {
    Flow[PkResult]
      .collect { case fkTask: FkTask => fkTask }
  }
}
