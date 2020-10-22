package trw.dbsubsetter.datacopy

import trw.dbsubsetter.db.{DbAccessFactory, DbVendor, SchemaInfo}

private[datacopy] trait DataCopier {
  def runTask(task: DataCopyTask): Unit
}

private[datacopy] object DataCopier {
  def from(dbAccessFactory: DbAccessFactory, schemaInfo: SchemaInfo): DataCopier = {
    dbAccessFactory.getDbVendor() match {
      case DbVendor.PostgreSQL => new DataCopierPostgresImpl(dbAccessFactory, schemaInfo)
      case _                   => new DataCopierGenericImpl(dbAccessFactory)
    }
  }
}
