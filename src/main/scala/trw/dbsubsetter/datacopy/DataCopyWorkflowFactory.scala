package trw.dbsubsetter.datacopy


import trw.dbsubsetter.datacopy.impl.{GenericDataCopyWorkflowImpl, PostgresOptimizedDataCopyWorkflowImpl}
import trw.dbsubsetter.db.{ConnectionFactory, DbAccessFactory, DbVendor, SchemaInfo}

object DataCopyWorkflowFactory {

  // scalastyle:off
  def build(dbAccessFactory: DbAccessFactory, connectionFactory: ConnectionFactory, originConnectionString: String, targetConnectionString: String, schemaInfo: SchemaInfo): DataCopyWorkflow = {
  // scalastyle:on
    val vendor: DbVendor = connectionFactory.getDbVendor(originConnectionString)

    vendor match {
      case DbVendor.PostgreSQL => new PostgresOptimizedDataCopyWorkflowImpl(connectionFactory, originConnectionString, targetConnectionString, schemaInfo)
      case _ => new GenericDataCopyWorkflowImpl(dbAccessFactory)
    }
  }

}
