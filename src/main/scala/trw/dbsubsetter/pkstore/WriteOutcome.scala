package trw.dbsubsetter.pkstore

sealed trait WriteOutcome
case object FirstTimeSeen extends WriteOutcome
case object AlreadySeenWithoutChildren extends WriteOutcome
case object AlreadySeenWithChildren extends WriteOutcome
