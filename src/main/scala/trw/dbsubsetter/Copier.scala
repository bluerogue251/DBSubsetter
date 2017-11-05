package trw.dbsubsetter

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.sql.Connection

import org.postgresql.PGConnection

object Copier {
  def copyToTargetDB(originConn: Connection, targetConn: Connection, pk: PrimaryKey, pkValues: scala.collection.Set[Vector[AnyRef]]): Unit = {
    val originCopyApi = originConn.asInstanceOf[PGConnection].getCopyAPI
    val targetCopyApi = targetConn.asInstanceOf[PGConnection].getCopyAPI
    val outputStream = new ByteArrayOutputStream()

    val copyOutStatement: SqlQuery =
      s"""copy (
         |        select ${pk.table.schema}.${pk.table.name}.*
         |        from ${pk.table.schema}.${pk.table.name}
         |        where (${pk.columns.map(_.name).mkString(", ")})
         |        in ( ${pkValues.map(pk => pk.mkString("('", "','", "')")).mkString(",\n")})
         |     )
         |to stdout
       """.stripMargin

    val copyInStatement: SqlQuery =
      s"""copy ${pk.table.schema}.${pk.table.name}
         |from stdin
       """.stripMargin

    originCopyApi.copyOut(copyOutStatement, outputStream)
    // Need to replace `toByteArray` with something that won't load everything into memory all at once
    targetCopyApi.copyIn(copyInStatement, new ByteArrayInputStream(outputStream.toByteArray))
  }
}
