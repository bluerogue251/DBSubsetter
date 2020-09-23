package trw.dbsubsetter.db

import java.sql.JDBCType

import trw.dbsubsetter.db.DbVendor.{MicrosoftSQLServer, MySQL, PostgreSQL}

object ColumnTypes {
  sealed trait ColumnType

  case object Short extends ColumnType
  case object Int extends ColumnType
  case object Long extends ColumnType
  case object BigInteger extends ColumnType
  case object String extends ColumnType
  case object ByteArray extends ColumnType
  case object Uuid extends ColumnType
  case class Unknown(description: String) extends ColumnType

  def fromRawInfo(jdbcType: JDBCType, typeName: String, vendor: DbVendor): ColumnType = {
    (jdbcType, typeName, vendor) match {
      case (JDBCType.TINYINT | JDBCType.SMALLINT, _, MicrosoftSQLServer)                    => ColumnTypes.Short
      case (JDBCType.INTEGER, "INT UNSIGNED", MySQL)                                        => ColumnTypes.Long
      case (JDBCType.TINYINT | JDBCType.SMALLINT | JDBCType.INTEGER, _, _)                  => ColumnTypes.Int
      case (JDBCType.BIGINT, "BIGINT UNSIGNED", MySQL)                                      => ColumnTypes.BigInteger
      case (JDBCType.BIGINT, _, _)                                                          => ColumnTypes.Long
      case (JDBCType.CHAR | JDBCType.VARCHAR | JDBCType.LONGVARCHAR | JDBCType.NCHAR, _, _) => ColumnTypes.String
      case (JDBCType.BINARY | JDBCType.VARBINARY | JDBCType.LONGVARBINARY, _, _)            => ColumnTypes.ByteArray
      case (_, "uuid", PostgreSQL)                                                          => ColumnTypes.Uuid
      case _                                                                                => ColumnTypes.Unknown(s"JDBC Type: $jdbcType, TypeName: $typeName")
    }
  }
}
