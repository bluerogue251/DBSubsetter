package trw.dbsubsetter.akkastreams

import akka.NotUsed
import akka.stream.scaladsl.Flow
import trw.dbsubsetter.db.SchemaInfo
import trw.dbsubsetter.workflow._

object PkAddedFlows {
  def pkAddedToNewTasksFlow(sch: SchemaInfo): Flow[PkResult, FkTask, NotUsed] = {
    Flow[PkResult]
      .mapConcat {
        case PksAdded(table, rows, fetchChildren) => RowsToTasksConverter.convert(table, rows, sch, fetchChildren)
        case _ => List.empty
      }
  }

  // TODO add parallelism and batching
  // TODO DRY up logic for getting PK value from a `Row`
  def pkAddedToDbCopyFlow(sch: SchemaInfo): Flow[PkResult, DbCopy, NotUsed] = {
    Flow[PkResult].mapConcat {
      case PksAdded(table, rows, _) =>
        rows.map { row =>
          val pkValue = sch.pksByTable(table).columns.map(row)
          DbCopy(sch.pksByTable(table), Set(pkValue))
        }
      case _ => List.empty
    }
  }
}
