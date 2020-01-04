package trw.dbsubsetter.datacopy


import trw.dbsubsetter.datacopy.impl.{GenericDataCopyWorkflowImpl, PostgresOptimizedDataCopyWorkflowImpl}
import trw.dbsubsetter.db.{DbAccessFactory, DbVendor, SchemaInfo}


object DataCopyWorkflowFactory {

  def build(dbAccessFactory: DbAccessFactory, schemaInfo: SchemaInfo): DataCopyWorkflow = {
    dbAccessFactory.getDbVendor() match {
      case DbVendor.PostgreSQL => new PostgresOptimizedDataCopyWorkflowImpl(dbAccessFactory, schemaInfo)
      case _ => new GenericDataCopyWorkflowImpl(dbAccessFactory)
    }
  }
}
