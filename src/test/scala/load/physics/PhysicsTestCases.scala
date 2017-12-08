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
  }

  val dataSetName = "physics"

  test("Correct research_institutions were included") {
    assertCount(ResearchInstitutions, 1)
    assertThat(ResearchInstitutions.map(_.id).sum.result, 1)
  }

  test("Correct research_groupss were included") {
    assertCount(ResearchGroups, 1)
    assertThat(ResearchGroups.map(_.id).sum.result, 1)
  }

  test("Correct scientists were included") {
    assertCount(Scientists, 1)
    assertThat(Scientists.map(_.id).sum.result, 1)
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
    assertCount(Experiments, 1)
    assertThat(Experiments.map(_.id).sum.result, 1)
  }

  test("Correct particle_domain rows were included") {
    assertCount(ParticleDomain, 1)
    assertThat(ParticleDomain.map(_.id).sum.result, 1)
  }

  test("Correct quantum_domain rows were included") {
    assertCount(QuantumDomain, 1)
    assertThat(QuantumDomain.map(_.id).sum.result, 1)
  }

  test("Correct gravitational_wave_domain rows were included") {
    assertCount(GravitationalWaveDomain, 1)
    assertThat(GravitationalWaveDomain.map(_.id).sum.result, 1)
  }

  test("Correct particle_collider_data rows were included") {
    assertCount(ParticleColliderData, 1)
    assertThatLong(ParticleColliderData.map(_.id).sum.result, 1)
  }

  test("Correct quantum_data rows were included") {
    assertCount(QuantumData, 1)
    assertThatLong(QuantumData.map(_.id).sum.result, 1)
  }

  test("Correct gravitational_wave_data rows were included") {
    assertCount(GravitationalWaveData, 1)
    assertThatLong(GravitationalWaveData.map(_.id).sum.result, 1)
  }
}
