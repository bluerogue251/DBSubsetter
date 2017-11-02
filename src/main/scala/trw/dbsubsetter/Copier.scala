package trw.dbsubsetter

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.sql.Connection

import org.postgresql.PGConnection

object Copier {
  def copyToTargetDB(originConn: Connection, targetConn: Connection, pkDefinition: PrimaryKey, pkValues: scala.collection.Set[Vector[AnyRef]]): Unit = {
    val originCopyApi = originConn.asInstanceOf[PGConnection].getCopyAPI
    val targetCopyApi = targetConn.asInstanceOf[PGConnection].getCopyAPI
    val outputStream = new ByteArrayOutputStream()

    val copyOutStatement: SqlQuery =
      s"""copy (
         |        select *
         |        from ${pkDefinition.tableSchema}.${pkDefinition.tableName}
         |        where (${pkDefinition.columns.map(_.name).mkString(", ")})
         |        in ( ${pkValues.map(pk => pk.mkString("('", "','", "')")).mkString(",\n")})
         |     )
         |to stdout
       """.stripMargin

    val copyInStatement: SqlQuery =
      s"""copy ${pkDefinition.tableSchema}.${pkDefinition.tableName}
         |from stdin
       """.stripMargin

    originCopyApi.copyOut(copyOutStatement, outputStream)
    // Need to replace `toByteArray` with something that won't load everything into memory all at once
    targetCopyApi.copyIn(copyInStatement, new ByteArrayInputStream(outputStream.toByteArray))
  }
}
