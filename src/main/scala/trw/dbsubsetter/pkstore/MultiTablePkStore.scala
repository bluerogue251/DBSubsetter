package trw.dbsubsetter.pkstore

import trw.dbsubsetter.db.{PrimaryKeyValue, Table}

private[pkstore] trait MultiTablePkStore {
  def markSeen(table: Table, value: PrimaryKeyValue): WriteOutcome
  def markSeenWithChildren(table: Table, value: PrimaryKeyValue): WriteOutcome
  def alreadySeen(table: Table, value: PrimaryKeyValue): Boolean
}

private[pkstore] object MultiTablePkStore {
  def from(tables: Seq[Table]): MultiTablePkStore = {
    val base: MultiTablePkStore = new MultiTablePkStoreImpl(tables)
    new MultiTablePkStoreInstrumentedImpl(base)
  }
}
