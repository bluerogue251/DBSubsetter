package e2e.missingfk

import e2e.AbstractMysqlEndToEndTest

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class MysqlTest extends AbstractMysqlEndToEndTest with TestCases {
  override val dataSetName = "missing_fk"
  override val originPort = 5490

  override val programArgs = Array(
    "--schemas", "missing_fk",
    "--baseQuery", "missing_fk.table_1 ::: id = 2 ::: true",
    "--foreignKey", "missing_fk.table_2(table_1_id) ::: missing_fk.table_1(id)",
    "--primaryKey", "missing_fk.table_4(table_1_id, table_3_id)"
  )

  override def insertOriginDbData(): Unit = {
    val db = slick.jdbc.MySQLProfile.backend.Database.forURL(singleThreadedConfig.originDbConnectionString)
    val fut = db.run(
      new Inserts(slick.jdbc.MySQLProfile) {
        override val profile = slick.jdbc.MySQLProfile
      }.dbioSeq
    )
    Await.result(fut, Duration.Inf)
  }
}
