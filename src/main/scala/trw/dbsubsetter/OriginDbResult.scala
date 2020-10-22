package trw.dbsubsetter

import trw.dbsubsetter.db.{Keys, Table}

case class OriginDbResult(table: Table, rows: Vector[Keys], viaTableOpt: Option[Table], fetchChildren: Boolean)
