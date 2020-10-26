package trw.dbsubsetter.pkstore

import trw.dbsubsetter.db.{PrimaryKeyValue, Table}

private[pkstore] final class MultiTablePkStoreInMemoryImpl(tables: Seq[Table]) extends MultiTablePkStore {

  private[this] val storesByTable: Map[Table, PkStore] =
    tables
      .map(table => table -> PkStore.empty())
      .toMap

  override def markSeen(table: Table, value: PrimaryKeyValue): WriteOutcome = {
    storesByTable(table).markSeen(value)
  }

  override def markSeenWithChildren(table: Table, value: PrimaryKeyValue): WriteOutcome = {
    storesByTable(table).markSeenWithChildren(value)
  }

  override def alreadySeen(table: Table, value: PrimaryKeyValue): Boolean = {
    storesByTable(table).alreadySeen(value)
  }
}
