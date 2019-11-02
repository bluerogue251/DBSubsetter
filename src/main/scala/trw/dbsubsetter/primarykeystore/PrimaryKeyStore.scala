package trw.dbsubsetter.primarykeystore

import trw.dbsubsetter.db.{PrimaryKeyValue, Table}

trait PrimaryKeyStore {
  def markSeen(table: Table, primaryKeyValue: Any): WriteOutcome
  def markSeenWithChildren(table: Table, primaryKeyValue: PrimaryKeyValue): WriteOutcome
  def alreadySeen(table: Table, primaryKeyValue: PrimaryKeyValue): Boolean
}

sealed trait WriteOutcome
case object FirstTimeSeen extends WriteOutcome
case object AlreadySeenWithoutChildren extends WriteOutcome
case object AlreadySeenWithChildren extends WriteOutcome
