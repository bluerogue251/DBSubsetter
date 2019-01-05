package trw.dbsubsetter.primarykeystore

import trw.dbsubsetter.db.SchemaInfo
import trw.dbsubsetter.primarykeystore.impl.InMemoryPrimaryKeyStore

object PrimaryKeyStoreFactory {

  /*
   * Only call this once per subset (it needs to be a singleton per subsetting run because it holds state)
   */
  def buildPrimaryKeyStore(schemaInfo: SchemaInfo): PrimaryKeyStore = {
    new InMemoryPrimaryKeyStore(schemaInfo)
  }
}
