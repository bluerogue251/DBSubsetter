package trw.dbsubsetter.primarykeystore

import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.SchemaInfo
import trw.dbsubsetter.primarykeystore.impl.{InMemoryPrimaryKeyStore, InstrumentedPrimaryKeyStore}

object PrimaryKeyStoreFactory {

  /*
   * Only call this once per subset (it needs to be a singleton per subsetting run because it holds state)
   */
  def buildPrimaryKeyStore(config: Config, schemaInfo: SchemaInfo): PrimaryKeyStore = {
    var primaryKeyStore: PrimaryKeyStore = new InMemoryPrimaryKeyStore(schemaInfo)

    if (config.exposeMetrics) {
      primaryKeyStore = new InstrumentedPrimaryKeyStore(primaryKeyStore)
    }

    primaryKeyStore
  }
}
