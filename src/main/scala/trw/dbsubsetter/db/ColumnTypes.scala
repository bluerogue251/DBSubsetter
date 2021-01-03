package trw.dbsubsetter.db

import trw.dbsubsetter.db.DbVendor.{MySQL, PostgreSQL}
import trw.dbsubsetter.db.value.ColumnValue

import java.sql.{JDBCType, ResultSet}
import java.util.UUID

object ColumnTypes {

  def extraction(jdbcType: JDBCType, typeName: String, vendor: DbVendor): Function[(String, ResultSet), ColumnValue] = {
    (jdbcType, typeName, vendor) match {
      case (JDBCType.INTEGER, "INT UNSIGNED", MySQL) => { case (columnName, resultSet) =>
        ColumnValue.long(resultSet.getLong(columnName))
      }

      case (JDBCType.BIGINT, "BIGINT UNSIGNED", MySQL) => { case (columnName, resultSet) =>
        ColumnValue.bigInt(resultSet.getObject(columnName, classOf[BigInt]))
      }

      case (JDBCType.TINYINT | JDBCType.SMALLINT | JDBCType.INTEGER, _, _) => { case (columnName, resultSet) =>
        ColumnValue.int(resultSet.getInt(columnName))
      }

      case (JDBCType.BIGINT, _, _) => { case (columnName, resultSet) =>
        ColumnValue.long(resultSet.getLong(columnName))
      }

      case (JDBCType.CHAR | JDBCType.VARCHAR | JDBCType.LONGVARCHAR | JDBCType.NCHAR, _, _) => {
        case (columnName, resultSet) =>
          ColumnValue.string(resultSet.getString(columnName))
      }

      case (JDBCType.BINARY | JDBCType.VARBINARY | JDBCType.LONGVARBINARY, _, _) => { case (columnName, resultSet) =>
        ColumnValue.bytes(resultSet.getBytes(columnName))
      }

      case (_, "uuid", PostgreSQL) => { case (columnName, resultSet) =>
        ColumnValue.uuid(resultSet.getObject(columnName, classOf[UUID]))
      }

      case _ => { case (columnName, resultSet) =>
        ColumnValue.unknown(resultSet.getObject(columnName))
      }
    }
  }
}
