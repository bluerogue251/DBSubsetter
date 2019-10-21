package trw.dbsubsetter.db

import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.impl.connection.ConnectionFactory
import trw.dbsubsetter.db.impl.mapper.{JdbcResultConverter, JdbcResultConverterImpl, JdbcResultConverterTimed}
import trw.dbsubsetter.db.impl.origin.{InstrumentedOriginDbAccess, OriginDbAccessImpl}
import trw.dbsubsetter.db.impl.target.{InstrumentedTargetDbAccess, TargetDbAccessImpl}

final class DbAccessFactory(config: Config, schemaInfo: SchemaInfo) {

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
      originDbAccess = new InstrumentedOriginDbAccess(originDbAccess)
    }

    originDbAccess
  }

  def buildTargetDbAccess(): TargetDbAccess = {
    var targetDbAccess: TargetDbAccess =
      new TargetDbAccessImpl(config.targetDbConnectionString, schemaInfo, connectionFactory)

    if (config.exposeMetrics) {
       targetDbAccess = new InstrumentedTargetDbAccess(targetDbAccess)
    }

    targetDbAccess
  }

  def closeAllConnections(): Unit = {
    connectionFactory.closeAllConnections()
  }
}
