package load.physics

import e2e.{AbstractEndToEndTest, SlickSetupDDL}
import util.assertion.AssertionUtil

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

trait PhysicsTestCases extends AbstractEndToEndTest with PhysicsDDL with SlickSetupDDL with AssertionUtil {

  import profile.api._

  override val ddl = schema.create

  override def setupOriginDML(): Unit = {
    val customDml = new PhysicsDML(profile)

    val dmlFut1 = originDb.run(customDml.initialInserts)
    Await.result(dmlFut1, Duration.Inf)
    val fut2 = originDb.run(DBIO.seq(customDml.particleDomainInserts: _*))
    Await.result(fut2, Duration.Inf)
    val fut3 = originDb.run(DBIO.seq(customDml.quantumDomainInserts: _*))
    Await.result(fut3, Duration.Inf)
    val fut4 = originDb.run(DBIO.seq(customDml.gravitationalWaveDomainInserts: _*))
    Await.result(fut4, Duration.Inf)
    val fut5 = originDb.run(DBIO.seq(customDml.particleColliderDataInserts: _*))
    Await.result(fut5, Duration.Inf)
    val fut6 = originDb.run(DBIO.seq(customDml.quantumDataInserts: _*))
    Await.result(fut6, Duration.Inf)
    val fut7 = originDb.run(DBIO.seq(customDml.gravitationalWaveDataInserts: _*))
    Await.result(fut7, Duration.Inf)

    import scala.concurrent.ExecutionContext.Implicits.global

    val pcFut = Future {
      (1 to customDml.numParticleColliderData by 50).foreach { i =>
        if ((i - 1) % 100000 == 0) println(s"ParticleCollider-$i")
        val inserts = (i to i + 49).map(i => customDml.particleColliderNotesInserts(i))
        val f = originDb.run(DBIO.seq(inserts: _*))
        Await.result(f, Duration.Inf)
      }
    }

    val qdFut = Future {
      (1 to customDml.numQuantumData by 50).foreach { i =>
        if ((i - 1) % 100000 == 0) println(s"Quantum-$i")
        val inserts = (i to i + 49).map(i => customDml.quantumNotesInserts(i))
        val f = originDb.run(DBIO.seq(inserts: _*))
        Await.result(f, Duration.Inf)
      }
    }

    val gwFut = Future {
      (1 to customDml.numGravitationalWaveData by 50).foreach { i =>
        if ((i - 1) % 100000 == 0) println(s"GravitationalWave-$i")
        val inserts = (i to i + 49).map(i => customDml.gravitationWaveNotesInserts(i))
        val f = originDb.run(DBIO.seq(inserts: _*))
        Await.result(f, Duration.Inf)
      }
    }

    Await.result(pcFut, Duration.Inf)
    Await.result(qdFut, Duration.Inf)
    Await.result(gwFut, Duration.Inf)
  }

  val dataSetName = "physics"

  test("Correct research_institutions were included") {
    assertCount(ResearchInstitutions, 1)
    assertThat(ResearchInstitutions.map(_.id).sum.result, 4)
  }

  test("Correct research_groups were included") {
    assertCount(ResearchGroups, 1)
    assertThat(ResearchGroups.map(_.id).sum.result, 3)
  }

  test("Correct scientists were included") {
    assertCount(Scientists, 1)
    assertThat(Scientists.map(_.id).sum.result, 2)
  }

  test("Correct experiment_metadata rows were included") {
    assertCount(ExperimentMetadata, 1)
    assertThat(ExperimentMetadata.map(_.id).sum.result, 1)
  }

  test("Correct experiment_plans were included") {
    assertCount(ExperimentPlans, 1)
    assertThat(ExperimentPlans.map(_.id).sum.result, 1)
  }

  test("Correct experiments were included") {
    assertCount(Experiments, 22)
    assertThat(Experiments.map(_.id).sum.result, 2277)
  }

  test("Correct particle_domain rows were included") {
    assertCount(ParticleDomain, 1000000)
    assertThatLong(ParticleDomain.map(_.id).sum.result, 500000500000l)
  }

  test("Correct quantum_domain rows were included") {
    assertCount(QuantumDomain, 1000000)
    assertThatLong(QuantumDomain.map(_.id).sum.result, 500000500000l)
  }

  test("Correct gravitational_wave_domain rows were included") {
    assertCount(GravitationalWaveDomain, 1000000)
    assertThatLong(GravitationalWaveDomain.map(_.id).sum.result, 500000500000l)
  }

  test("Correct particle_collider_data rows were included") {
    assertCount(ParticleColliderData, 550000)
    assertThatLong(ParticleColliderData.map(_.id).sum.result, 1402500275000l)
  }

  test("Correct quantum_data rows were included") {
    assertCount(QuantumData, 660000)
    assertThatLong(QuantumData.map(_.id).sum.result, 2019600330000l)
  }

  test("Correct gravitational_wave_data rows were included") {
    assertCount(GravitationalWaveData, 440000)
    assertThatLong(GravitationalWaveData.map(_.id).sum.result, 897600220000l)
  }

  test("Correct datum_notes were included") {
    assertCount(DatumNotes, 21010000)
    assertThatLong(DatumNotes.map(_.id).sum.result, 2006021074421801l)
  }
}