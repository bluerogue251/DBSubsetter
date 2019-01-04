package trw.dbsubsetter.primarykeystore

import trw.dbsubsetter.db.Table

trait PrimaryKeyStore {
  def markSeen(table: Table, primaryKeyValue: Any): Boolean
  def markSeenWithChildren(table: Table, primaryKeyValue: Any): Boolean
  def alreadySeen(table: Table, primaryKeyValue: Any): Boolean
  def alreadySeenWithChildren(table: Table, primaryKeyValue: Any): Boolean
}
