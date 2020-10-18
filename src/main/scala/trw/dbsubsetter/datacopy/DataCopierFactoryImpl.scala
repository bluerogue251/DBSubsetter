package trw.dbsubsetter.datacopy

import trw.dbsubsetter.datacopy.impl.{GenericDataCopierImpl, PostgresDataCopierImpl}
import trw.dbsubsetter.db.{DbAccessFactory, DbVendor, SchemaInfo}

final class DataCopierFactoryImpl(dbAccessFactory: DbAccessFactory, schemaInfo: SchemaInfo) extends DataCopierFactory {

  def build(): DataCopier = {
    dbAccessFactory.getDbVendor() match {
      case DbVendor.PostgreSQL => new PostgresDataCopierImpl(dbAccessFactory, schemaInfo)
      case _                   => new GenericDataCopierImpl(dbAccessFactory)
    }
  }
}
