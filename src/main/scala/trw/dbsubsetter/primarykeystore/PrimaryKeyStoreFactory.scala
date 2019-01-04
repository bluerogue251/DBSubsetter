package trw.dbsubsetter.primarykeystore

import trw.dbsubsetter.db.SchemaInfo
import trw.dbsubsetter.primarykeystore.impl.InMemoryPrimaryKeyStore

object PrimaryKeyStoreFactory {

  private[this] var lazyInitializedPrimaryKeyStoreSingleton: PrimaryKeyStore = _

  def getPrimaryKeyStore(schemaInfo: SchemaInfo): PrimaryKeyStore = {
    if (lazyInitializedPrimaryKeyStoreSingleton == null) {
      lazyInitializedPrimaryKeyStoreSingleton = new InMemoryPrimaryKeyStore(schemaInfo)
    }
    lazyInitializedPrimaryKeyStoreSingleton
  }
}
