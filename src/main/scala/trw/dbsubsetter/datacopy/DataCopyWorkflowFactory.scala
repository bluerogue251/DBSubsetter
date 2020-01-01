package trw.dbsubsetter.datacopy


import trw.dbsubsetter.datacopy.impl.{GenericDataCopyWorkflowImpl, PostgresOptimizedDataCopyWorkflowImpl}
import trw.dbsubsetter.db.{ConnectionFactory, DbAccessFactory, DbVendor}

object DataCopyWorkflowFactory {

  def build(dbAccessFactory: DbAccessFactory, connectionFactory: ConnectionFactory, connectionString: String): DataCopyWorkflow = {
    val vendor: DbVendor = connectionFactory.getDbVendor(connectionString)

    vendor match {
      case DbVendor.PostgreSQL => new PostgresOptimizedDataCopyWorkflowImpl(connectionFactory)
      case _ => new GenericDataCopyWorkflowImpl(dbAccessFactory)
    }
  }

}
