package e2e

import slick.dbio.{DBIOAction, Effect, NoStream}
import util.db.Database

import scala.concurrent.Await
import scala.concurrent.duration.Duration

trait SlickSetupDML[T <: Database] extends AbstractEndToEndTest[T] {
  val dml: DBIOAction[Unit, NoStream, Effect.Write]

  override protected def prepareOriginDML(): Unit = {
    val dmlFut = originSlick.run(dml)
    Await.result(dmlFut, Duration.Inf)
  }
}
