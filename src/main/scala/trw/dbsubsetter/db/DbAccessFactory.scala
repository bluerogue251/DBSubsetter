package trw.dbsubsetter.db

import java.sql.Connection

import org.postgresql.copy.CopyManager
import org.postgresql.core.BaseConnection
import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.impl.mapper.{JdbcResultConverter, JdbcResultConverterImpl, JdbcResultConverterTimed}
import trw.dbsubsetter.db.impl.origin.{InstrumentedOriginDbAccess, OriginDbAccessImpl}
import trw.dbsubsetter.db.impl.target.{InstrumentedTargetDbAccess, TargetDbAccessImpl}

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
    connectionFactory.getDbVendor(config.originDbConnectionString) match {
      case DbVendor.PostgreSQL =>
        val connection: Connection = connectionFactory.getReadOnlyConnection(config.originDbConnectionString)
        new CopyManager(connection.asInstanceOf[BaseConnection])
      case _ =>
        throw new RuntimeException("Postgres COPY not supported for this database")
    }
  }

  def buildTargetPostgresCopyManager(): CopyManager = {
    connectionFactory.getDbVendor(config.targetDbConnectionString) match {
      case DbVendor.PostgreSQL =>
        val connection: Connection = connectionFactory.getReadWriteConnection(config.targetDbConnectionString)
        new CopyManager(connection.asInstanceOf[BaseConnection])
      case _ =>
        throw new RuntimeException("Postgres COPY not supported for this database")
    }
  }

  def closeAllConnections(): Unit = {
    connectionFactory.closeAllConnections()
  }
}
