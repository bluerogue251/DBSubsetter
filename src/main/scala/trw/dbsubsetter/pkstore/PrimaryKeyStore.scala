package trw.dbsubsetter.pkstore

import trw.dbsubsetter.db.{MultiColumnPrimaryKeyValue, Table}

private[pkstore] trait PrimaryKeyStore {
  def markSeen(table: Table, primaryKeyValue: MultiColumnPrimaryKeyValue): WriteOutcome
  def markSeenWithChildren(table: Table, primaryKeyValue: MultiColumnPrimaryKeyValue): WriteOutcome
  def alreadySeen(table: Table, primaryKeyValue: MultiColumnPrimaryKeyValue): Boolean
}

private[pkstore] object PrimaryKeyStore {
  def from(tables: Seq[Table]): PrimaryKeyStore = {
    val base: PrimaryKeyStore = new PrimaryKeyStoreInMemoryImpl(tables)
    new PrimaryKeyStoreInstrumentedImpl(base)
  }
}
