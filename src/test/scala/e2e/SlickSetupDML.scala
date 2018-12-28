package e2e

import slick.dbio.{DBIOAction, Effect, NoStream}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

trait SlickSetupDML {

  protected val profile: slick.jdbc.JdbcProfile

  protected def originSlick: profile.backend.DatabaseDef

  protected def dml: DBIOAction[Unit, NoStream, Effect.Write]

  protected def prepareOriginDML(): Unit = {
    val dmlFut = originSlick.run(dml)
    Await.result(dmlFut, Duration.Inf)
  }
}
