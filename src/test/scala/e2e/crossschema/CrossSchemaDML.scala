package e2e.crossschema

import slick.jdbc.JdbcProfile

class CrossSchemaDML(val profile: JdbcProfile) extends CrossSchemaDDL {

  import profile.api._

  def dbioSeq = {
    slick.dbio.DBIO.seq(
      Schema1Table ++= Seq(
        Schema1TableRow(1),
        Schema1TableRow(2),
        Schema1TableRow(3)
      ),
      Schema2Table ++= Seq(
        Schema2TableRow(1, 1),
        Schema2TableRow(2, 2),
        Schema2TableRow(3, 3)
      ),
      Schema3Table ++= Seq(
        Schema3TableRow(1, 1),
        Schema3TableRow(2, 2),
        Schema3TableRow(3, 3),
        Schema3TableRow(4, 1),
        Schema3TableRow(5, 2),
        Schema3TableRow(6, 3)
      )
    )
  }
}
