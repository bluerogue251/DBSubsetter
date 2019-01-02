package trw.dbsubsetter.db

import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.impl._

class DbAccessFactory(config: Config, schemaInfo: SchemaInfo) {

  private val connectionFactory = new ConnectionFactory()

  def buildOriginDbAccess(): OriginDbAccess = {
    var originDbAccess: OriginDbAccess =
      new OriginDbAccessImpl(config.originDbConnectionString, schemaInfo, connectionFactory)

    if (config.exposePrometheusMetrics) {
      originDbAccess = new OriginDbAccessTimed(originDbAccess)
    }

    originDbAccess
  }

  def buildTargetDbAccess(): TargetDbAccess = {
    var targetDbAccess: TargetDbAccess =
      new TargetDbAccessImpl(config.targetDbConnectionString, schemaInfo, connectionFactory)

    if (config.exposePrometheusMetrics) {
       targetDbAccess = new TargetDbAccessTimed(targetDbAccess)
    }

    targetDbAccess
  }

  def closeAllConnections(): Unit = {
    connectionFactory.closeAllConnections()
  }
}