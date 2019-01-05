package trw.dbsubsetter.db

import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.impl.connection.ConnectionFactory
import trw.dbsubsetter.db.impl.mapper.{JdbcResultConverter, JdbcResultConverterImpl, JdbcResultConverterTimed}
import trw.dbsubsetter.db.impl.origin.{OriginDbAccessImpl, OriginDbAccessTimed}
import trw.dbsubsetter.db.impl.target.{TargetDbAccessImpl, TargetDbAccessTimed}

class DbAccessFactory(config: Config, schemaInfo: SchemaInfo) {

  private[this] val connectionFactory: ConnectionFactory = new ConnectionFactory

  def buildOriginDbAccess(): OriginDbAccess = {
    var mapper: JdbcResultConverter =
      new JdbcResultConverterImpl(schemaInfo)

    if (config.exposeMetrics) {
      mapper = new JdbcResultConverterTimed(mapper)
    }

    var originDbAccess: OriginDbAccess =
      new OriginDbAccessImpl(config.originDbConnectionString, schemaInfo, mapper, connectionFactory)

    if (config.exposeMetrics) {
      originDbAccess = new OriginDbAccessTimed(originDbAccess)
    }

    originDbAccess
  }

  def buildTargetDbAccess(): TargetDbAccess = {
    var targetDbAccess: TargetDbAccess =
      new TargetDbAccessImpl(config.targetDbConnectionString, schemaInfo, connectionFactory)

    if (config.exposeMetrics) {
       targetDbAccess = new TargetDbAccessTimed(targetDbAccess)
    }

    targetDbAccess
  }

  def closeAllConnections(): Unit = {
    connectionFactory.closeAllConnections()
  }
}