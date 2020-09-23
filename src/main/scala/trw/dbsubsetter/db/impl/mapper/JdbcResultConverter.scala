package trw.dbsubsetter.db.impl.mapper

import java.sql.ResultSet

import trw.dbsubsetter.db.Keys
import trw.dbsubsetter.db.Row
import trw.dbsubsetter.db.Table

private[db] trait JdbcResultConverter {
  def convertToKeys(res: ResultSet, table: Table): Vector[Keys]
  def convertToRows(res: ResultSet, table: Table): Vector[Row]
}
