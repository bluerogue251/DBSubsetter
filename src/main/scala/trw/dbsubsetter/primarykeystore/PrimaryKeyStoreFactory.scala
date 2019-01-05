package trw.dbsubsetter.primarykeystore

import trw.dbsubsetter.db.SchemaInfo
import trw.dbsubsetter.primarykeystore.impl.InMemoryPrimaryKeyStore

/*
 * Will this being a singleton mess up e2e test suite where multiple subsets happen in the same process?
 */
object PrimaryKeyStoreFactory {

  private[this] var lazyPrimaryKeyStoreSingleton: PrimaryKeyStore = _

  def getPrimaryKeyStore(schemaInfo: SchemaInfo): PrimaryKeyStore = {
    if (lazyPrimaryKeyStoreSingleton == null) {
      lazyPrimaryKeyStoreSingleton = new InMemoryPrimaryKeyStore(schemaInfo)
    }
    lazyPrimaryKeyStoreSingleton
  }
}
