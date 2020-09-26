package trw.dbsubsetter.db

import java.sql.Connection

import org.postgresql.copy.CopyManager
import org.postgresql.core.BaseConnection
import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.impl.ConnectionFactory
import trw.dbsubsetter.db.impl.mapper.{JdbcResultConverter, JdbcResultConverterImpl, JdbcResultConverterInstrumented}
import trw.dbsubsetter.db.impl.origin.{InstrumentedOriginDbAccess, OriginDbAccessImpl}
import trw.dbsubsetter.db.impl.target.{InstrumentedTargetDbAccess, TargetDbAccessImpl}

final class DbAccessFactory(config: Config, schemaInfo: SchemaInfo) {

  private[this] val connectionFactory: ConnectionFactory = new ConnectionFactory

  def getDbVendor(): DbVendor = {
    connectionFactory.getDbVendor(config.originDbConnectionString)
  }

  def buildOriginDbAccess(): OriginDbAccess = {
    val baseMapper: JdbcResultConverter =
      new JdbcResultConverterImpl(schemaInfo)

    val instrumentedMapper: JdbcResultConverter =
      new JdbcResultConverterInstrumented(baseMapper)

    val baseOriginDbAccess: OriginDbAccess =
      new OriginDbAccessImpl(
        config.originDbConnectionString,
        schemaInfo,
        instrumentedMapper,
        connectionFactory
      )

    new InstrumentedOriginDbAccess(baseOriginDbAccess)
  }

  def buildTargetDbAccess(): TargetDbAccess = {
    val base: TargetDbAccess =
      new TargetDbAccessImpl(
        config.targetDbConnectionString,
        schemaInfo,
        connectionFactory
      )
    new InstrumentedTargetDbAccess(base)
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
