package e2e

import slick.dbio.{DBIOAction, Effect, NoStream}
import slick.jdbc.JdbcBackend

import scala.concurrent.Await
import scala.concurrent.duration.Duration

trait SlickSetupDDL {

  protected val profile: slick.jdbc.JdbcProfile

  protected def originSlick: JdbcBackend#DatabaseDef

  protected def ddl: DBIOAction[Unit, NoStream, Effect.Schema]

  protected def prepareOriginDDL(): Unit = {
    val ddlFut = originSlick.run(ddl)
    Await.result(ddlFut, Duration.Inf)
  }
}
