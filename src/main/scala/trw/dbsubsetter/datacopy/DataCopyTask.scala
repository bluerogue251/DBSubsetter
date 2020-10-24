package trw.dbsubsetter.datacopy

import trw.dbsubsetter.db.{MultiColumnPrimaryKeyValue, Table}

class DataCopyTask(val table: Table, val pkValues: Seq[MultiColumnPrimaryKeyValue])
