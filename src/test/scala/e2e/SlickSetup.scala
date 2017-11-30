package e2e

import slick.dbio.{DBIOAction, Effect, NoStream}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

trait SlickSetup extends AbstractEndToEndTest {
  val ddl: DBIOAction[Unit, NoStream, Effect.Schema]

  val dml: DBIOAction[Unit, NoStream, Effect.Write]

  override def setupDDL(): Unit = {
    val ddlFut = originDb.run(ddl)
    Await.result(ddlFut, Duration.Inf)
  }

  override def setupDML(): Unit = {
    val dmlFut = originDb.run(dml)
    Await.result(dmlFut, Duration.Inf)
  }
}
