package trw.dbsubsetter.datacopy

import trw.dbsubsetter.db.{DbAccessFactory, DbVendor, SchemaInfo}

private[datacopy] final class DataCopierFactoryImpl(dbAccessFactory: DbAccessFactory, schemaInfo: SchemaInfo)
    extends DataCopierFactory {

  def build(): DataCopier = {
    dbAccessFactory.getDbVendor() match {
      case DbVendor.PostgreSQL => new DataCopierPostgresImpl(dbAccessFactory, schemaInfo)
      case _                   => new DataCopierGenericImpl(dbAccessFactory)
    }
  }
}
