package trw.dbsubsetter.akkastreams

import akka.NotUsed
import akka.stream.scaladsl.Flow
import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.{SchemaInfo, TargetDbAccess}
import trw.dbsubsetter.workflow._

object TargetDbInsertFlow {
  def flow(config: Config, schemaInfo: SchemaInfo): Flow[TargetDbInsertRequest, TargetDbInsertResult, NotUsed] = {
    Flow[TargetDbInsertRequest].statefulMapConcat { () =>
      val db = new TargetDbAccess(config.targetDbConnectionString, schemaInfo)

      req => {
        val rowsInserted = db.insertRows(req.table, req.rows)
        val result = TargetDbInsertResult(req.table, rowsInserted)
        List(result)
      }
    }
  }
}
