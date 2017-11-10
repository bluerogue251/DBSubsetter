package trw.dbsubsetter.akkastreams

import akka.NotUsed
import akka.stream.scaladsl.Flow
import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.{DbAccess, SchemaInfo}
import trw.dbsubsetter.workflow._

object DbCallFlow {
  def flow(config: Config, schemaInfo: SchemaInfo): Flow[DbRequest, DbFetchResult, NotUsed] = {
    Flow[DbRequest].statefulMapConcat { () =>
      val db = new DbAccess(config.originDbConnectionString, config.targetDbConnectionString, schemaInfo)
      req => {
        req match {
          case FkQuery(t: FkTask) =>
            val rows = db.getRowsFromTemplate(t.fk, t.table, t.fetchChildren, t.values)
            List(DbFetchResult(t.table, rows, t.fetchChildren))
          case SqlStrQuery(table, columns, sql) =>
            List(DbFetchResult(table, db.getRows(sql, columns), fetchChildren = true))
          case DbCopy(pk, pkValues) =>
            db.copyToTargetDB(pk, pkValues)
            List.empty
        }
      }
    }
  }
}
