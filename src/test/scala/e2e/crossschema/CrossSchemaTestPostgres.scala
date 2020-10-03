package e2e.crossschema

import e2e.PostgresSubsettingTest
import slick.dbio.DBIO
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class CrossSchemaTestPostgres extends PostgresSubsettingTest with CrossSchemaTest {
  // format: off

  override val programArgs = Array(
    "--schemas", "schema_1, schema_2, schema_3",
    "--baseQuery", "schema_1.schema_1_table ::: id = 2 ::: includeChildren"
  )

  override def prepareOriginDDL(): Unit = {
    val createSchemaStatements: DBIO[Unit] = DBIO.seq(
      sqlu"create schema schema_1",
      sqlu"create schema schema_2",
      sqlu"create schema schema_3"
    )
    Await.ready(originSlick.run(createSchemaStatements), Duration.Inf)
    super.prepareOriginDDL()
  }
}