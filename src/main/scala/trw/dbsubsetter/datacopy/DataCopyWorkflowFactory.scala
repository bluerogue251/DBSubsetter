package trw.dbsubsetter.datacopy

import trw.dbsubsetter.datacopy.impl.GenericDataCopyWorkflowImpl
import trw.dbsubsetter.datacopy.impl.PostgresOptimizedDataCopyWorkflowImpl
import trw.dbsubsetter.db.DbAccessFactory
import trw.dbsubsetter.db.DbVendor
import trw.dbsubsetter.db.SchemaInfo

object DataCopyWorkflowFactory {

  def build(dbAccessFactory: DbAccessFactory, schemaInfo: SchemaInfo): DataCopyWorkflow = {
    dbAccessFactory.getDbVendor() match {
      case DbVendor.PostgreSQL => new PostgresOptimizedDataCopyWorkflowImpl(dbAccessFactory, schemaInfo)
      case _                   => new GenericDataCopyWorkflowImpl(dbAccessFactory)
    }
  }
}
