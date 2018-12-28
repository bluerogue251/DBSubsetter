package e2e

import slick.dbio.{DBIOAction, Effect, NoStream}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

trait SlickSetupDDL[T] extends AbstractEndToEndTest[T] {
  val ddl: DBIOAction[Unit, NoStream, Effect.Schema]

  override protected def prepareOriginDDL(): Unit = {
    val ddlFut = originSlick.run(ddl)
    Await.result(ddlFut, Duration.Inf)
  }
}
