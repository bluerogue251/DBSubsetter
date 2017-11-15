package trw.dbsubsetter.akkastreams

import akka.NotUsed
import akka.stream.scaladsl.Flow
import trw.dbsubsetter.db.SchemaInfo
import trw.dbsubsetter.workflow._

object PkResultFlows {
  def pkAddedToNewTasks(sch: SchemaInfo): Flow[PkResult, FkTask, NotUsed] = {
    Flow[PkResult]
      .mapConcat {
        case PksAdded(table, rows, fetchChildren) => RowsToTasksConverter.convert(table, rows, sch, fetchChildren)
        case _ => List.empty
      }
  }

  // TODO add parallelism and batching
  // TODO DRY up logic for getting PK value from a `Row`
  def pkAddedToDbInsert(sch: SchemaInfo): Flow[PkResult, TargetDbInsertRequest, NotUsed] = {
    Flow[PkResult]
      .collect { case PksAdded(table, rows, _) => TargetDbInsertRequest(table, rows) }
  }

  def pkMissingToFkQuery: Flow[PkResult, FkTask, NotUsed] = {
    Flow[PkResult]
      .collect { case fkTask: FkTask => fkTask }
  }
}
