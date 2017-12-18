package e2e

import slick.dbio.{DBIOAction, Effect, NoStream}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

trait SlickSetupDDL extends AbstractEndToEndTest {
  val ddl: DBIOAction[Unit, NoStream, Effect.Schema]

  override def setupOriginDDL(): Unit = {
    val ddlFut = originDb.run(ddl)
    Await.result(ddlFut, Duration.Inf)
  }
}
