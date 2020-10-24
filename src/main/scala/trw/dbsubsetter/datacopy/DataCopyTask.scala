package trw.dbsubsetter.datacopy

import trw.dbsubsetter.db.{PrimaryKeyValue, Table}

class DataCopyTask(val table: Table, val pkValues: Seq[PrimaryKeyValue])
