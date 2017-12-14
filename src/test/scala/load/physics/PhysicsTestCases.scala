package load.physics

import e2e.{AbstractEndToEndTest, SlickSetupDDL}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

trait PhysicsTestCases extends AbstractEndToEndTest with PhysicsDDL with SlickSetupDDL {

  import profile.api._

  override val ddl = schema.create

  override def setupDML(): Unit = {
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

    (1 to customDml.numParticleColliderData by 2074).foreach { i =>
      val f = originDb.run(DBIO.seq(customDml.particleColliderNotesInserts(i)))
      Await.result(f, Duration.Inf)
    }

    (1 to customDml.numQuantumData by 1210).foreach { i =>
      val f = originDb.run(DBIO.seq(customDml.quantumNotesInserts(i)))
      Await.result(f, Duration.Inf)
    }

    (1 to customDml.numGravitationalWaveData by 501).foreach { i =>
      val f = originDb.run(DBIO.seq(customDml.gravitationWaveNotesInserts(i)))
      Await.result(f, Duration.Inf)
    }
  }

  val dataSetName = "physics"

  test("Correct research_institutions were included") {
    assertCount(ResearchInstitutions, 9)
    assertThat(ResearchInstitutions.map(_.id).sum.result, 45)
  }

  test("Correct research_groups were included") {
    assertCount(ResearchGroups, 29)
    assertThat(ResearchGroups.map(_.id).sum.result, 435)
  }

  test("Correct scientists were included") {
    assertCount(Scientists, 50)
    assertThat(Scientists.map(_.id).sum.result, 2550)
  }

  test("Correct experiment_metadata rows were included") {
    assertCount(ExperimentMetadata, 5)
    assertThat(ExperimentMetadata.map(_.id).sum.result, 25)
  }

  test("Correct experiment_plans were included") {
    assertCount(ExperimentPlans, 5)
    assertThat(ExperimentPlans.map(_.id).sum.result, 25)
  }

  test("Correct experiments were included") {
    assertCount(Experiments, 111)
    assertThat(Experiments.map(_.id).sum.result, 11233)
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
    assertCount(ParticleColliderData, 2775000)
    assertThatLong(ParticleColliderData.map(_.id).sum.result, 6917188887500l)
  }

  test("Correct quantum_data rows were included") {
    assertCount(QuantumData, 3330000)
    assertThatLong(QuantumData.map(_.id).sum.result, 9960751665000l)
  }

  test("Correct gravitational_wave_data rows were included") {
    assertCount(GravitationalWaveData, 2220000)
    assertThatLong(GravitationalWaveData.map(_.id).sum.result, 4427001110000l)
  }

  test("Correct datum_notes were included") {
    assertCount(DatumNotes, 50000000)
    assertThatLong(DatumNotes.map(_.id).sum.result, 1l)
  }
}