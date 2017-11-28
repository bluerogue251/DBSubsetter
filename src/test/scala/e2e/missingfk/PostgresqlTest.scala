package e2e.missingfk

import e2e.AbstractPostgresqlEndToEndTest

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class PostgresqlTest extends AbstractPostgresqlEndToEndTest with TestCases {
  override val dataSetName = "missing_fk"
  override val originPort = 5493

  override val programArgs = Array(
    "--schemas", "public",
    "--baseQuery", "public.table_1 ::: id = 2 ::: true",
    "--foreignKey", "public.table_2(table_1_id) ::: public.table_1(id)",
    "--primaryKey", "public.table_4(table_1_id, table_3_id)"
  )

  override def insertOriginDbData(): Unit = {
    val db = slick.jdbc.PostgresProfile.backend.Database.forURL(singleThreadedConfig.originDbConnectionString)
    val fut = db.run(
      new Inserts(slick.jdbc.PostgresProfile) {
        override val profile = slick.jdbc.PostgresProfile
      }.dbioSeq
    )
    Await.result(fut, Duration.Inf)
  }
}
