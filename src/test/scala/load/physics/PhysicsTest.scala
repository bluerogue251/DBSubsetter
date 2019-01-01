package load.physics

import org.scalatest.FunSuiteLike
import util.assertion.AssertionUtil
import util.slick.SlickUtil

import scala.concurrent.Await
import scala.concurrent.duration.Duration

trait PhysicsTest extends FunSuiteLike with AssertionUtil {
  protected val testName = "physics"

  protected val profile: slick.jdbc.JdbcProfile

  protected def originSlick: slick.jdbc.JdbcBackend#DatabaseDef

  private val ddl: PhysicsDDL = new PhysicsDDL(profile)

  import ddl.profile.api._

  protected def prepareOriginDDL(): Unit = {
    SlickUtil.ddl(originSlick, ddl.schema.create)
  }

  protected def prepareOriginDML(): Unit = {
    val customDml = new PhysicsDML(ddl)

    val dmlFut1 = originSlick.run(customDml.initialInserts)
    Await.result(dmlFut1, Duration.Inf)
    println("Done with fut1" + System.currentTimeMillis())

    val fut2 = originSlick.run(DBIO.seq(customDml.particleDomainInserts: _*))
    Await.result(fut2, Duration.Inf)
    println("Done with fut2" + System.currentTimeMillis())

    val fut3 = originSlick.run(DBIO.seq(customDml.quantumDomainInserts: _*))
    Await.result(fut3, Duration.Inf)
    println("Done with fut3" + System.currentTimeMillis())

    val fut4 = originSlick.run(DBIO.seq(customDml.gravitationalWaveDomainInserts: _*))
    Await.result(fut4, Duration.Inf)
    println("Done with fut4" + System.currentTimeMillis())

    val fut5 = originSlick.run(DBIO.seq(customDml.particleColliderDataInserts: _*))
    Await.result(fut5, Duration.Inf)
    println("Done with fut5" + System.currentTimeMillis())

    val fut6 = originSlick.run(DBIO.seq(customDml.quantumDataInserts: _*))
    Await.result(fut6, Duration.Inf)
    println("Done with fut6" + System.currentTimeMillis())

    val fut7 = originSlick.run(DBIO.seq(customDml.gravitationalWaveDataInserts: _*))
    Await.result(fut7, Duration.Inf)
    println("Done with fut7" + System.currentTimeMillis())

    val batchSize = 10000

    (1 to customDml.numParticleColliderData by batchSize).foreach { i =>
      println(s"ParticleCollider-$i-" + System.currentTimeMillis())
      val inserts = customDml.particleColliderNotesInserts(i, batchSize)
      val f = originSlick.run(DBIO.seq(inserts))
      Await.result(f, Duration.Inf)
    }

    (1 to customDml.numQuantumData by batchSize).foreach { i =>
      println(s"Quantum-$i-" + System.currentTimeMillis())
      val inserts = customDml.quantumNotesInserts(i, batchSize)
      val f = originSlick.run(DBIO.seq(inserts))
      Await.result(f, Duration.Inf)
    }

   (1 to customDml.numGravitationalWaveData by batchSize).foreach { i =>
     println(s"GravitationalWave-$i-" + System.currentTimeMillis())
     val inserts = customDml.gravitationWaveNotesInserts(i, batchSize)
     val f = originSlick.run(DBIO.seq(inserts))
     Await.result(f, Duration.Inf)
   }
  }

  test("Correct research_institutions were included") {
    assertCount(ddl.ResearchInstitutions, 1)
    assertThat(ddl.ResearchInstitutions.map(_.id).sum.result, 4)
  }

  test("Correct research_groups were included") {
    assertCount(ddl.ResearchGroups, 1)
    assertThat(ddl.ResearchGroups.map(_.id).sum.result, 3)
  }

  test("Correct scientists were included") {
    assertCount(ddl.Scientists, 1)
    assertThat(ddl.Scientists.map(_.id).sum.result, 2)
  }

  test("Correct experiment_metadata rows were included") {
    assertCount(ddl.ExperimentMetadata, 1)
    assertThat(ddl.ExperimentMetadata.map(_.id).sum.result, 1)
  }

  test("Correct experiment_plans were included") {
    assertCount(ddl.ExperimentPlans, 1)
    assertThat(ddl.ExperimentPlans.map(_.id).sum.result, 1)
  }

  test("Correct experiments were included") {
    assertCount(ddl.Experiments, 22)
    assertThat(ddl.Experiments.map(_.id).sum.result, 2277)
  }

  test("Correct particle_domain rows were included") {
    assertCount(ddl.ParticleDomain, 1000000)
    assertThatLong(ddl.ParticleDomain.map(_.id).sum.result, 500000500000l)
  }

  test("Correct quantum_domain rows were included") {
    assertCount(ddl.QuantumDomain, 1000000)
    assertThatLong(ddl.QuantumDomain.map(_.id).sum.result, 500000500000l)
  }

  test("Correct gravitational_wave_domain rows were included") {
    assertCount(ddl.GravitationalWaveDomain, 1000000)
    assertThatLong(ddl.GravitationalWaveDomain.map(_.id).sum.result, 500000500000l)
  }

  test("Correct particle_collider_data rows were included") {
    assertCount(ddl.ParticleColliderData, 550000)
    assertThatLong(ddl.ParticleColliderData.map(_.id).sum.result, 1402500275000l)
  }

  test("Correct quantum_data rows were included") {
    assertCount(ddl.QuantumData, 660000)
    assertThatLong(ddl.QuantumData.map(_.id).sum.result, 2019600330000l)
  }

  test("Correct gravitational_wave_data rows were included") {
    assertCount(ddl.GravitationalWaveData, 440000)
    assertThatLong(ddl.GravitationalWaveData.map(_.id).sum.result, 897600220000l)
  }

  test("Correct datum_notes were included") {
    assertCount(ddl.DatumNotes, 21010000)
    assertThatLong(ddl.DatumNotes.map(_.id).sum.result, 2219314170745000l)
  }

  test("Correct datum_note_responses were included") {
    assertCount(ddl.DatumNoteResponses, 0)
  }
}