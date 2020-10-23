package trw.dbsubsetter.pkstore

import trw.dbsubsetter.db.{PrimaryKeyValue, SchemaInfo, Table}

private[pkstore] trait PrimaryKeyStore {
  def markSeen(table: Table, primaryKeyValue: PrimaryKeyValue): WriteOutcome
  def markSeenWithChildren(table: Table, primaryKeyValue: PrimaryKeyValue): WriteOutcome
  def alreadySeen(table: Table, primaryKeyValue: PrimaryKeyValue): Boolean
}

private[pkstore] object PrimaryKeyStore {
  def from(schemaInfo: SchemaInfo): PrimaryKeyStore = {
    val base: PrimaryKeyStore = new PrimaryKeyStoreInMemoryImpl(schemaInfo)
    new PrimaryKeyStoreInstrumentedImpl(base)
  }
}
