package trw.dbsubsetter.db

trait OriginDbAccess {
  def getRowsFromForeignKeyValue(fk: ForeignKey, table: Table, fkValue: Any): Vector[Row]
  def getRowsFromPrimaryKeyValues(table: Table, primaryKeyValues: Seq[PrimaryKeyValue]): Vector[Row]
  def getRows(query: SqlQuery, table: Table): Vector[Row]
}
