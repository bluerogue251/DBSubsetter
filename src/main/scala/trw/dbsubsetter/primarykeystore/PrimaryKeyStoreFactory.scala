package trw.dbsubsetter.primarykeystore

import trw.dbsubsetter.db.SchemaInfo
import trw.dbsubsetter.primarykeystore.impl.InMemoryPrimaryKeyStore

object PrimaryKeyStoreFactory {

  private[this] var lazyPrimaryKeyStoreSingleton: PrimaryKeyStore = _

  def getPrimaryKeyStore(schemaInfo: SchemaInfo): PrimaryKeyStore = {
    if (lazyPrimaryKeyStoreSingleton == null) {
      lazyPrimaryKeyStoreSingleton = new InMemoryPrimaryKeyStore(schemaInfo)
    }
    lazyPrimaryKeyStoreSingleton
  }
}
