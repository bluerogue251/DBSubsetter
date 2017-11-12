package trw.dbsubsetter.akkastreams

import akka.NotUsed
import akka.stream.scaladsl.Flow
import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.{OriginDbAccess, SchemaInfo}
import trw.dbsubsetter.workflow._

object OriginDbQueryFlow {
  def flow(config: Config, schemaInfo: SchemaInfo): Flow[OriginDbRequest, OriginDbResult, NotUsed] = {
    Flow[OriginDbRequest].statefulMapConcat { () =>
      val db = new OriginDbAccess(config.originDbConnectionString, schemaInfo)
      req => {
        req match {
          case FkQuery(t: FkTask) =>
            val rows = db.getRowsFromTemplate(t.fk, t.table, t.values)
            List(OriginDbResult(t.table, rows, t.fetchChildren))
          case SqlStrQuery(table, sql) =>
            val rows = db.getRows(sql, table)
            List(OriginDbResult(table, rows, fetchChildren = true))
        }
      }
    }
  }
}
