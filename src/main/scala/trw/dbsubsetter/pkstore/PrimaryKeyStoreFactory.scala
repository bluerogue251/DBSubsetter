package trw.dbsubsetter.pkstore

import trw.dbsubsetter.db.SchemaInfo

object PrimaryKeyStoreFactory {

  /*
   * Only call this once per subset (it needs to be a singleton per subsetting run because it holds state)
   */
  def buildPrimaryKeyStore(schemaInfo: SchemaInfo): PrimaryKeyStore = {
    val base: PrimaryKeyStore = new PrimaryKeyStoreInMemoryImpl(schemaInfo)
    new PrimaryKeyStoreInstrumentedImpl(base)
  }
}
