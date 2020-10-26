package trw.dbsubsetter.pkstore

import trw.dbsubsetter.db.{PrimaryKeyValue, Table}

private[pkstore] trait MultiTablePkStore {
  def markSeen(table: Table, primaryKeyValue: PrimaryKeyValue): WriteOutcome
  def markSeenWithChildren(table: Table, primaryKeyValue: PrimaryKeyValue): WriteOutcome
  def alreadySeen(table: Table, primaryKeyValue: PrimaryKeyValue): Boolean
}

private[pkstore] object MultiTablePkStore {
  def from(tables: Seq[Table]): MultiTablePkStore = {
    val base: MultiTablePkStore = new MultiTablePkStoreImpl(tables)
    new MultiTablePkStoreInstrumentedImpl(base)
  }
}
