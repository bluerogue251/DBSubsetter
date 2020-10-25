package trw.dbsubsetter.pkstore

import trw.dbsubsetter.db.{PrimaryKeyValue, Table}

private[pkstore] final class PrimaryKeyStoreImpl(tables: Seq[Table]) extends PrimaryKeyStore {

  private[this] val storesByTable: Map[Table, PrimaryKeyStoreSingleTable] =
    tables
      .map(table => table -> new PrimaryKeyStoreSingleTableImpl())
      .toMap

  override def markSeen(table: Table, primaryKeyValue: PrimaryKeyValue): WriteOutcome = {
    storesByTable(table).markSeen(primaryKeyValue)
  }

  override def markSeenWithChildren(table: Table, primaryKeyValue: PrimaryKeyValue): WriteOutcome = {
    storesByTable(table).markSeenWithChildren(primaryKeyValue)
  }

  override def alreadySeen(table: Table, primaryKeyValue: PrimaryKeyValue): Boolean = {
    storesByTable(table).alreadySeen(primaryKeyValue)
  }
}
