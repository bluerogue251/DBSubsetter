package e2e.autoincrementingpk

import scala.concurrent.Await
import scala.concurrent.duration.Duration

trait OriginDBSetup {

  protected val profile: slick.jdbc.JdbcProfile

  protected def originSlick: profile.backend.DatabaseDef

  protected val ddl: DDL = new DDL(profile)

  protected val dml: DML = new DML(ddl)

  protected def prepareOriginDDL(): Unit = {
    import ddl.profile.api._
    val ddlFut = originSlick.run(ddl.schema.create)
    Await.result(ddlFut, Duration.Inf)
  }

  protected def prepareOriginDML(): Unit = {
    val ddlFut = originSlick.run(dml.dbioSeq)
    Await.result(ddlFut, Duration.Inf)
  }
}
