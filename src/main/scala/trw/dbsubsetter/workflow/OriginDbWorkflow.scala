package trw.dbsubsetter.workflow

import io.prometheus.client.Histogram
import io.prometheus.client.Histogram.Timer
import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.{ConnectionFactory, OriginDbAccess, SchemaInfo}


class OriginDbWorkflow(config: Config, schemaInfo: SchemaInfo, connectionFactory: ConnectionFactory) {
  private[this] val db = new OriginDbAccess(config.originDbConnectionString, schemaInfo, connectionFactory)

  def process(request: OriginDbRequest): OriginDbResult = {
    val timer: Timer = OriginDbWorkflow.histogram.startTimer()
    val result = request match {
      case FkTask(table, foreignKey, fkValue, fetchChildren) =>
        val rows = db.getRowsFromTemplate(foreignKey, table, fkValue)
        val viaTableOpt = if (fetchChildren) Some(foreignKey.toTable) else None
        OriginDbResult(table, rows, viaTableOpt, fetchChildren)
      case SqlStrQuery(table, sql, fetchChildren) =>
        val rows = db.getRows(sql, table)
        OriginDbResult(table, rows, None, fetchChildren)
    }
    timer.observeDuration()
    result
  }
}

object OriginDbWorkflow {
  private val histogram: Histogram = Histogram
    .build()
    .name("OriginDbWorkflow")
    .help("n/a")
    .register()
}