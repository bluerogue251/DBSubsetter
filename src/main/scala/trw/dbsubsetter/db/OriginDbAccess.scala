package trw.dbsubsetter.db

trait OriginDbAccess {
  def getRowsFromForeignKeyValue(fk: ForeignKey, table: Table, fkValue: ForeignKeyValue): Vector[Keys]
  def getRowsFromPrimaryKeyValues(table: Table, primaryKeyValues: Seq[MultiColumnPrimaryKeyValue]): Vector[Row]
  def getRowsFromWhereClause(table: Table, whereClause: String): Vector[Keys]
}
