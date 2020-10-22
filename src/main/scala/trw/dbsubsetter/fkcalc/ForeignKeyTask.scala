package trw.dbsubsetter.fkcalc

import trw.dbsubsetter.db.{ForeignKey, ForeignKeyValue}

sealed trait ForeignKeyTask
case class FetchParentTask(fk: ForeignKey, fkValueFromChild: ForeignKeyValue) extends ForeignKeyTask
case class FetchChildrenTask(fk: ForeignKey, fkValueFromParent: ForeignKeyValue) extends ForeignKeyTask
