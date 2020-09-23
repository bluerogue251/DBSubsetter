package trw.dbsubsetter.db

import java.sql.Connection

import org.postgresql.copy.CopyManager
import org.postgresql.core.BaseConnection
import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.impl.ConnectionFactory
import trw.dbsubsetter.db.impl.mapper.JdbcResultConverter
import trw.dbsubsetter.db.impl.mapper.JdbcResultConverterImpl
import trw.dbsubsetter.db.impl.mapper.JdbcResultConverterTimed
import trw.dbsubsetter.db.impl.origin.InstrumentedOriginDbAccess
import trw.dbsubsetter.db.impl.origin.OriginDbAccessImpl
import trw.dbsubsetter.db.impl.target.InstrumentedTargetDbAccess
import trw.dbsubsetter.db.impl.target.TargetDbAccessImpl

final class DbAccessFactory(config: Config, schemaInfo: SchemaInfo) {

  private[this] val connectionFactory: ConnectionFactory = new ConnectionFactory

  def getDbVendor(): DbVendor = {
    connectionFactory.getDbVendor(config.originDbConnectionString)
  }

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

  def buildOriginPostgresCopyManager(): CopyManager = {
    val connection: Connection = connectionFactory.getReadOnlyConnection(config.originDbConnectionString)
    new CopyManager(connection.asInstanceOf[BaseConnection])
  }

  def buildTargetPostgresCopyManager(): CopyManager = {
    val connection: Connection = connectionFactory.getReadWriteConnection(config.targetDbConnectionString)
    new CopyManager(connection.asInstanceOf[BaseConnection])
  }

  def closeAllConnections(): Unit = {
    connectionFactory.closeAllConnections()
  }
}
