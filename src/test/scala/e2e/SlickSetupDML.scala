package e2e

import slick.dbio.{DBIOAction, Effect, NoStream}
import slick.jdbc.JdbcBackend

import scala.concurrent.Await
import scala.concurrent.duration.Duration

trait SlickSetupDML {

  protected def originSlick: JdbcBackend#DatabaseDef

  protected def dml: DBIOAction[Unit, NoStream, Effect.Write]

  protected def prepareOriginDML(): Unit = {
    val dmlFut = originSlick.run(dml)
    Await.result(dmlFut, Duration.Inf)
  }
}
