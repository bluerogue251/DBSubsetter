package trw.dbsubsetter.workflow

import trw.dbsubsetter.db._

object RawTaskToForeignKeyTaskMapper {

  def map(rawForeignKey: ForeignKey, rawFetchChildren: Boolean, rawForeignKeyValue: Any): ForeignKeyTask = {
    if (rawFetchChildren) {
      FetchChildrenTask(
        childTable = rawForeignKey.fromTable,
        viaParentTable = rawForeignKey.toTable,
        fk = rawForeignKey,
        fkValueFromParent = rawForeignKeyValue
      )
    } else {
      FetchParentTask(
        parentTable = rawForeignKey.toTable,
        fk = rawForeignKey,
        fkValueFromChild = rawForeignKeyValue
      )
    }
  }
}

