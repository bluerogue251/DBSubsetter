package e2e

import slick.dbio.{DBIOAction, Effect, NoStream}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

trait SlickSetupDDL {

  protected val profile: slick.jdbc.JdbcProfile

  protected def originSlick: profile.backend.DatabaseDef

  protected def ddl: DBIOAction[Unit, NoStream, Effect.Schema]

  protected def prepareOriginDDL(): Unit = {
    val ddlFut = originSlick.run(ddl)
    Await.result(ddlFut, Duration.Inf)
  }
}
