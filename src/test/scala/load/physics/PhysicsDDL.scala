package load.physics

trait PhysicsDDL {
  val profile: slick.jdbc.JdbcProfile

  import profile.api._

  lazy val schema: profile.SchemaDescription = Array(ResearchInstitutions.schema).reduceLeft(_ ++ _)

  case class ResearchInstitution(id: Int, name: String, createdAt: java.sql.Timestamp)

  class ResearchInstitutions(_tableTag: Tag) extends profile.api.Table[ResearchInstitution](_tableTag, "research_institutions") {
    def * = (id, name, createdAt) <> (ResearchInstitution.tupled, ResearchInstitution.unapply)

    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    val name: Rep[String] = column[String]("name")
    val createdAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created_at")
  }

  lazy val ResearchInstitutions = new TableQuery(tag => new ResearchInstitutions(tag))

  case class ResearchGroup(id: Int, name: String, researchInstitutionId: Int, createdAt: java.sql.Timestamp)

  class ResearchGroups(_tableTag: Tag) extends profile.api.Table[ResearchGroup](_tableTag, "research_groups") {
    def * = (id, name, researchInstitutionId, createdAt) <> (ResearchGroup.tupled, ResearchGroup.unapply)

    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    val name: Rep[String] = column[String]("name")
    val researchInstitutionId: Rep[Int] = column[Int]("research_institution_id")
    val createdAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created_at")
  }

  lazy val ResearchGroups = new TableQuery(tag => new ResearchGroups(tag))

  case class Scientist(id: Int, name: String, researchGroupId: Option[Int], createdAt: java.sql.Timestamp)

  class Scientists(_tableTag: Tag) extends profile.api.Table[Scientist](_tableTag, "scientists") {
    def * = (id, name, researchGroupId, createdAt) <> (Scientist.tupled, Scientist.unapply)

    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    val name: Rep[String] = column[String]("name")
    val researchGroupId: Rep[Option[Int]] = column[Option[[Int]]("research_group_id")
    val createdAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created_at")
  }

  lazy val Scientists = new TableQuery(tag => new Scientists(tag))

  case class ExperimentMetadataRow(id: Int, metadata: String, scientistId: Option[Int], createdAt: java.sql.Timestamp)

  class ExperimentMetadata(_tableTag: Tag) extends profile.api.Table[ExperimentMetadataRow](_tableTag, "experiment_metadata") {
    def * = (id, metadata, scientistId, createdAt) <> (ExperimentMetadataRow.tupled, ExperimentMetadataRow.unapply)

    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    val metadata: Rep[String] = column[String]("metadata")
    val scientistId: Rep[Option[Int]] = column[Option[[Int]]("scientist_id")
    val createdAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created_at")
  }

  lazy val ExperimentMetadata = new TableQuery(tag => new ExperimentMetadata(tag))

  case class ExperimentPlan(id: Int, planDescription: String, scientistId: Int, createdAt: java.sql.Timestamp)

  class ExperimentPlans(_tableTag: Tag) extends profile.api.Table[ExperimentPlan](_tableTag, "experiment_plans") {
    def * = (id, planDescription, scientistId, createdAt) <> (ExperimentPlan.tupled, ExperimentPlan.unapply)

    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    val planDescription: Rep[String] = column[String]("planDescription")
    val scientistId: Rep[Int] = column[Int]("scientist_id")
    val createdAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created_at")
  }

  lazy val ExperimentPlans = new TableQuery(tag => new ExperimentPlans(tag))

  case class Experiment(id: Int, notes: String, experimentPlanId: Int, createdAt: java.sql.Timestamp)

  class Experiments(_tableTag: Tag) extends profile.api.Table[Experiment](_tableTag, "experiments") {
    def * = (id, notes, experimentId, createdAt) <> (Experiment.tupled, Experiment.unapply)

    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    val notes: Rep[String] = column[String]("notes")
    val experimentId: Rep[Int] = column[Int]("experiment_id")
    val createdAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created_at")
  }

  lazy val Experiments = new TableQuery(tag => new Experiments(tag))

  case class ParticleDomainRow(id: Int, domainData: String, createdAt: java.sql.Timestamp)

  class ParticleDomain(_tableTag: Tag) extends profile.api.Table[ParticleDomainRow](_tableTag, "particle_domain") {
    def * = (id, domainData, createdAt) <> (ParticleDomainRow.tupled, ParticleDomainRow.unapply)

    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    val domainData: Rep[String] = column[String]("data")
    val createdAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created_at")
  }

  lazy val ParticleDomain = new TableQuery(tag => new ParticleDomain(tag))

  case class QuantumDomainRow(id: Int, domainData: String, createdAt: java.sql.Timestamp)

  class QuantumDomain(_tableTag: Tag) extends profile.api.Table[QuantumDomainRow](_tableTag, "quantum_domain") {
    def * = (id, domainData, createdAt) <> (QuantumDomainRow.tupled, QuantumDomainRow.unapply)

    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    val domainData: Rep[String] = column[String]("data")
    val createdAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created_at")
  }

  lazy val QuantumDomain = new TableQuery(tag => new QuantumDomain(tag))

  case class GravitationalWaveDomainRow(id: Int, domainData: String, createdAt: java.sql.Timestamp)

  class GravitationalWaveDomain(_tableTag: Tag) extends profile.api.Table[GravitationalWaveDomainRow](_tableTag, "gravitational_wave_domain") {
    def * = (id, domainData, createdAt) <> (GravitationalWaveDomainRow.tupled, GravitationalWaveDomainRow.unapply)

    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    val domainData: Rep[String] = column[String]("data")
    val createdAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created_at")
  }

  lazy val GravitationalWaveDomain = new TableQuery(tag => new GravitationalWaveDomain(tag))

  case class ParticleColliderDataRow(id: Long, experimentId: Int, particleDomainId: Int, data1: Double, data2: Double, data3: Double, data4: Double, data5: Double, data6: Long, data7: Long, data8: Long, data9: String, data10: String, data11: String, createdAt: java.sql.Timestamp)

  class ParticleColliderData(_tableTag: Tag) extends profile.api.Table[ParticleColliderDataRow](_tableTag, "particle_collider_data") {
    def * = (id, experimentId, particleDomainId, data1, data2, data3, data4, data5, data6, data7, data8, data9, data10, data11, createdAt) <> (ParticleColliderDataRow.tupled, ParticleColliderDataRow.unapply)

    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    val experimentId: Rep[Int] = column[Int]("experiment_id")
    val particleDomainId: Rep[Int] = column[Int]("particle_domain_data_id")
    val data1: Rep[Double] = column[Double]("data_1")
    val data2: Rep[Double] = column[Double]("data_2")
    val data3: Rep[Double] = column[Double]("data_3")
    val data4: Rep[Double] = column[Double]("data_4")
    val data5: Rep[Double] = column[Double]("data_5")
    val data6: Rep[Long] = column[Long]("data_6")
    val data7: Rep[Long] = column[Long]("data_7")
    val data8: Rep[Long] = column[Long]("data_8")
    val data9: Rep[String] = column[String]("data_9")
    val data10: Rep[String] = column[String]("data_10")
    val data11: Rep[String] = column[String]("data_11")
    val createdAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created_at")
  }

  lazy val ParticleColliderData = new TableQuery(tag => new ParticleColliderData(tag))

  case class QuantumDataRow(id: Long, experimentId: Int, quantumDomainId: Int, data1: String, data2: String, data3: String, createdAt: java.sql.Timestamp)

  class QuantumData(_tableTag: Tag) extends profile.api.Table[QuantumDataRow](_tableTag, "quantum_data") {
    def * = (id, experimentId, quantumDomainId, data1, data2, data3, createdAt) <> (QuantumDataRow.tupled, QuantumDataRow.unapply)

    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    val experimentId: Rep[Int] = column[Int]("experiment_id")
    val quantumDomainId: Rep[Int] = column[Int]("quantum_domain_data_id")
    val data1: Rep[String] = column[String]("data_1")
    val data2: Rep[String] = column[String]("data_2")
    val data3: Rep[String] = column[String]("data_3")
    val createdAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created_at")
  }

  lazy val QuantumData = new TableQuery(tag => new QuantumData(tag))

  case class GravitationalWaveDataRow(id: Long, experimentId: Int, gravitationalWaveDomainId: Int, data: String, createdAt: java.sql.Timestamp)

  class GravitationalWaveData(_tableTag: Tag) extends profile.api.Table[GravitationalWaveDataRow](_tableTag, "gravitational_wave_data") {
    def * = (id, experimentId, quantumDomainId, data, createdAt) <> (GravitationalWaveDataRow.tupled, GravitationalWaveDataRow.unapply)

    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    val experimentId: Rep[Int] = column[Int]("experiment_id")
    val quantumDomainId: Rep[Int] = column[Int]("quantum_domain_data_id")
    val data: Rep[String] = column[String]("data")
    val createdAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created_at")
  }

  lazy val GravitationalWaveData = new TableQuery(tag => new GravitationalWaveData(tag))
}
