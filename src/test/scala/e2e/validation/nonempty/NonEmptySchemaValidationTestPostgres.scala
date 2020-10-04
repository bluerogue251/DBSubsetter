package e2e.validation.nonempty

import e2e.PostgresEnabledTest
import slick.dbio.DBIO
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class NonEmptySchemaValidationTestPostgres extends PostgresEnabledTest with NonEmptySchemaValidationTest {

  override def prepareOriginDDL(): Unit = {
    val createSchemaStatements: DBIO[Unit] = DBIO.seq(
      sqlu"create schema valid_schema"
    )
    Await.ready(originSlick.run(createSchemaStatements), Duration.Inf)
    super.prepareOriginDDL()
  }

}
