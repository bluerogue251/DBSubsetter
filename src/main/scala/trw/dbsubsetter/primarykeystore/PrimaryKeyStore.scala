package trw.dbsubsetter.primarykeystore

import trw.dbsubsetter.db.Table

trait PrimaryKeyStore {
  def markSeen(table: Table, primaryKeyValue: Any): Boolean
  def markSeenWithChildren(table: Table, primaryKeyValue: Any): WriteOutcome
  def alreadySeen(table: Table, primaryKeyValue: Any): Boolean
  def alreadySeenWithChildren(table: Table, primaryKeyValue: Any): Boolean
}

sealed trait WriteOutcome
case object FirstTimeSeen extends WriteOutcome
case object AlreadySeenWithoutChildren extends WriteOutcome
case object AlreadySeenWithChildren extends WriteOutcome
