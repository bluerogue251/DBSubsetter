package trw.dbsubsetter.db

trait OriginDbAccess {
  def getRowsFromTemplate(fk: ForeignKey, table: Table, fkValue: Any): Vector[Row]
  def getRows(query: SqlQuery, table: Table): Vector[Row]
}
