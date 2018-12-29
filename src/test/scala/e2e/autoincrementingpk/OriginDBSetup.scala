package e2e.autoincrementingpk

import scala.concurrent.Await
import scala.concurrent.duration.Duration

trait OriginDBSetup {

  protected def profile: slick.jdbc.JdbcProfile

  protected def originSlick: slick.jdbc.JdbcBackend#DatabaseDef

  protected val ddl: DDL = new DDL(profile)

  protected def prepareOriginDDL(): Unit = {
    import ddl.profile.api._
    val ddlFuture = originSlick.run(ddl.schema.create)
    Await.ready(ddlFuture, Duration.Inf)
  }

  protected def prepareOriginDML(): Unit = {
    val dmlDbioAction = DML.dbioSeq(ddl)
    val dmlFuture = originSlick.run(dmlDbioAction)
    Await.ready(dmlFuture, Duration.Inf)
  }
}
