package e2e.schooldb

import slick.jdbc.JdbcProfile

class SchoolDBDML(val profile: JdbcProfile) extends SchoolDbDDL {

  def dbioSeq = {
    slick.dbio.DBIO.seq()
  }
}
