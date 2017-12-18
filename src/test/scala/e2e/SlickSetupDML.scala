package e2e

import slick.dbio.{DBIOAction, Effect, NoStream}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

trait SlickSetupDML extends AbstractEndToEndTest {
  val dml: DBIOAction[Unit, NoStream, Effect.Write]

  override def setupOriginDML(): Unit = {
    val dmlFut = originDb.run(dml)
    Await.result(dmlFut, Duration.Inf)
  }
}
