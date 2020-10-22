package trw.dbsubsetter.pkstore

import trw.dbsubsetter.fkcalc.FetchParentTask

sealed trait PkQueryResult
case object AlreadySeen extends PkQueryResult
case class NotAlreadySeen(task: FetchParentTask) extends PkQueryResult
