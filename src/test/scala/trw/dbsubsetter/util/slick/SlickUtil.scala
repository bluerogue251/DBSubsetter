package trw.dbsubsetter.util.slick

import slick.dbio.{DBIOAction, Effect, NoStream}
import slick.sql.FixedSqlAction

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object SlickUtil {
  def ddl(db: slick.jdbc.JdbcBackend#DatabaseDef, ddl: FixedSqlAction[Unit, NoStream, Effect.Schema]): Unit = {
    Await.ready(db.run(ddl), Duration.Inf)
  }

  def dml(db: slick.jdbc.JdbcBackend#DatabaseDef, dml: DBIOAction[Unit, NoStream, Effect.Write]): Unit = {
    Await.ready(db.run(dml), Duration.Inf)
  }
}
