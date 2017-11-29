package e2e.missingfk

import e2e.AbstractMysqlEndToEndTest

class MissingFkMysqlTest extends AbstractMysqlEndToEndTest with MissingFkTestCases {
  override val profile = slick.jdbc.MySQLProfile

  import profile.api._

  override val ddl = schema.create
  override val dml = new MissingFkDML(profile).dbioSeq
  override val dataSetName = "missing_fk"
  override val originPort = 5490
  override val programArgs = Array(
    "--schemas", "missing_fk",
    "--baseQuery", "missing_fk.table_1 ::: id = 2 ::: true",
    "--foreignKey", "missing_fk.table_2(table_1_id) ::: missing_fk.table_1(id)",
    "--primaryKey", "missing_fk.table_4(table_1_id, table_3_id)"
  )
}
