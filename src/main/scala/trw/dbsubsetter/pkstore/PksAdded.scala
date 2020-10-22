package trw.dbsubsetter.pkstore

import trw.dbsubsetter.db.{Keys, Table}

case class PksAdded(
    table: Table,
    rowsNeedingParentTasks: Vector[Keys],
    rowsNeedingChildTasks: Vector[Keys],
    viaTableOpt: Option[Table]
)
