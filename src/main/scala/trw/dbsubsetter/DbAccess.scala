package trw.dbsubsetter

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.sql.{PreparedStatement, ResultSet}

import org.postgresql.PGConnection

import scala.collection.mutable.ArrayBuffer

// Put the result in a collection of Maps from column names to values, each element in the collection is a row of the result
// Could we be more efficient by doing this by index rather than by column name?
class DbAccess(originConnStr: String, targetConnStr: String, sqlTemplates: SqlTemplates) {


  def getRows(preparedStatment: PreparedStatement, params: Seq[AnyRef], cols: Seq[Column]): Seq[Row] = {
    params.zipWithIndex.foreach { case (value, i) =>
      preparedStatment.setObject(i + 1, value)
    }
    val jdbcResult = preparedStatment.executeQuery()
    preparedStatment.clearParameters()
    jdbcResultToRows(jdbcResult, cols)
  }

  def getRows(query: SqlQuery, cols: Seq[Column]): Seq[Row] = {
    val jdbcResult = originConn.createStatement().executeQuery(query)
    jdbcResultToRows(jdbcResult, cols)
  }

  def copyToTargetDB(pk: PrimaryKey, pkValues: scala.collection.Set[Vector[AnyRef]]): Unit = {
    val originCopyApi = originConn.asInstanceOf[PGConnection].getCopyAPI
    val targetCopyApi = targetConn.asInstanceOf[PGConnection].getCopyAPI
    val outputStream = new ByteArrayOutputStream()

    val copyOutStatement: SqlQuery =
      s"""copy (
         |        select ${pk.table.fullyQualifiedName}.*
         |        from ${pk.table.fullyQualifiedName}
         |        where (${pk.columns.map(_.fullyQualifiedName).mkString(", ")})
         |        in ( ${pkValues.map(pk => pk.mkString("('", "','", "')")).mkString(",\n")})
         |     )
         |to stdout
       """.stripMargin

    val copyInStatement: SqlQuery = s"copy ${pk.table.fullyQualifiedName} from stdin"

    originCopyApi.copyOut(copyOutStatement, outputStream)
    // Need to replace `toByteArray` with something that won't load everything into memory all at once
    targetCopyApi.copyIn(copyInStatement, new ByteArrayInputStream(outputStream.toByteArray))
  }

  private def jdbcResultToRows(res: ResultSet, cols: Seq[Column]): Seq[Row] = {
    // Could we avoid using ArrayBuffer by knowing up front how many rows were fetched from DB?
    val rows = ArrayBuffer.empty[Row]
    while (res.next()) {
      rows += cols.map(col => col -> res.getObject(col.name)).toMap
    }
    rows
  }
}