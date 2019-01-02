package trw.dbsubsetter.db.impl.mapper

import java.sql.ResultSet

import trw.dbsubsetter.db.{Row, Table}

private[db] trait JdbcResultConverter {
  def convert(res: ResultSet, table: Table): Vector[Row]
}
