package trw.dbsubsetter.workflow

import trw.dbsubsetter.db._

object RawTaskToForeignKeyTaskMapper {

  def map(rawForeignKey: ForeignKey, rawFetchChildren: Boolean, rawForeignKeyValue: Any): ForeignKeyTask = {
    if (rawFetchChildren) {
      FetchChildrenTask(rawForeignKey.fromTable, rawForeignKey, rawForeignKeyValue)
    } else {
      FetchParentTask(rawForeignKey.toTable, rawForeignKey, rawForeignKeyValue)
    }
  }
}

