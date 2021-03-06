package util.assertion

import org.scalatest.Assertions
import slick.dbio.{DBIOAction, Effect}
import slick.jdbc.JdbcBackend
import slick.lifted.{AbstractTable, TableQuery}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

trait AssertionUtil extends Assertions {

  protected val profile: slick.jdbc.JdbcProfile

  protected def targetSlick: JdbcBackend#DatabaseDef

  final def assertCount[T <: AbstractTable[_]](tq: TableQuery[T], expected: Long): Unit = {
    import profile.api._
    assert(Await.result(targetSlick.run(tq.size.result), Duration.Inf) === expected)
  }

  // Helper to get around intelliJ warnings, technically it could compile just with the Long version
  final def assertThat(action: DBIOAction[Option[Int], profile.api.NoStream, Effect.Read], expected: Long): Unit = {
    assert(Await.result(targetSlick.run(action), Duration.Inf) === Some(expected))
  }

  final def assertResult[T](action: DBIOAction[T, profile.api.NoStream, Effect.Read], expected: T): Unit = {
    assert(Await.result(targetSlick.run(action), Duration.Inf) === expected)
  }

  final def assertThatLong(
      action: DBIOAction[Option[Long], profile.api.NoStream, Effect.Read],
      expected: Long
  ): Unit = {
    assert(Await.result(targetSlick.run(action), Duration.Inf) === Some(expected))
  }
}
