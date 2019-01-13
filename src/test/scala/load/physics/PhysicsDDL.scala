package load.physics

class PhysicsDDL(val profile: slick.jdbc.JdbcProfile) {
  import profile.api._

  lazy val schema: profile.SchemaDescription = Array(
    ResearchInstitutions.schema,
    ResearchGroups.schema,
    Scientists.schema,
    ExperimentMetadata.schema,
    ExperimentPlans.schema,
    Experiments.schema,
    ParticleDomain.schema,
    QuantumDomain.schema,
    GravitationalWaveDomain.schema,
    ParticleColliderData.schema,
    QuantumData.schema,
    GravitationalWaveData.schema,
    DatumNotes.schema,
    DatumNoteResponses.schema
  ).reduceLeft(_ ++ _)

  case class ResearchInstitution(id: Int, name: String, createdAt: java.sql.Timestamp)

  class ResearchInstitutions(_tableTag: Tag) extends Table[ResearchInstitution](_tableTag, "research_institutions") {
    def * = (id, name, createdAt) <> (ResearchInstitution.tupled, ResearchInstitution.unapply)

    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    val name: Rep[String] = column[String]("name")
    val createdAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created_at")
  }

  lazy val ResearchInstitutions = new TableQuery(tag => new ResearchInstitutions(tag))

  case class ResearchGroup(id: Int, name: String, researchInstitutionId: Int, createdAt: java.sql.Timestamp)

  class ResearchGroups(_tableTag: Tag) extends Table[ResearchGroup](_tableTag, "research_groups") {
    def * = (id, name, researchInstitutionId, createdAt) <> (ResearchGroup.tupled, ResearchGroup.unapply)

    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    val name: Rep[String] = column[String]("name")
    val researchInstitutionId: Rep[Int] = column[Int]("research_institution_id")
    val createdAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created_at")

    lazy val fk1 = foreignKey("research_groups_research_institution_id_fkey", researchInstitutionId, ResearchInstitutions)(_.id)
    val idx1 = index("research_groups_research_institution_id_idx", researchInstitutionId)
  }

  lazy val ResearchGroups = new TableQuery(tag => new ResearchGroups(tag))

  case class Scientist(id: Int, name: String, researchGroupId: Option[Int], createdAt: java.sql.Timestamp)

  class Scientists(_tableTag: Tag) extends Table[Scientist](_tableTag, "scientists") {
    def * = (id, name, researchGroupId, createdAt) <> (Scientist.tupled, Scientist.unapply)

    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    val name: Rep[String] = column[String]("name")
    val researchGroupId: Rep[Option[Int]] = column[Option[Int]]("research_group_id")
    val createdAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created_at")

    lazy val fk1 = foreignKey("scientists_research_group_id_fkey", researchGroupId, ResearchGroups)(_.id.?)
    val idx1 = index("scientists_research_group_id_idx", researchGroupId)
  }

  lazy val Scientists = new TableQuery(tag => new Scientists(tag))

  case class ExperimentMetadataRow(id: Int, metadata: String, scientistId: Int, createdAt: java.sql.Timestamp)

  class ExperimentMetadata(_tableTag: Tag) extends Table[ExperimentMetadataRow](_tableTag, "experiment_metadata") {
    def * = (id, metadata, scientistId, createdAt) <> (ExperimentMetadataRow.tupled, ExperimentMetadataRow.unapply)

    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    val metadata: Rep[String] = column[String]("metadata")
    val scientistId: Rep[Int] = column[Int]("scientist_id")
    val createdAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created_at")

    lazy val fk1 = foreignKey("experiment_metadata_scientist_id_fkey", scientistId, Scientists)(_.id)
    val idx1 = index("experiment_metadata_scientist_id_idx", scientistId)
  }

  lazy val ExperimentMetadata = new TableQuery(tag => new ExperimentMetadata(tag))

  case class ExperimentPlan(id: Int, planDescription: String, scientistId: Int, metadataId: Int, createdAt: java.sql.Timestamp)

  class ExperimentPlans(_tableTag: Tag) extends Table[ExperimentPlan](_tableTag, "experiment_plans") {
    def * = (id, planDescription, scientistId, metadataId, createdAt) <> (ExperimentPlan.tupled, ExperimentPlan.unapply)

    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    val planDescription: Rep[String] = column[String]("planDescription")
    val scientistId: Rep[Int] = column[Int]("scientist_id")
    val metadataId: Rep[Int] = column[Int]("metadata_id")
    val createdAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created_at")

    lazy val fk1 = foreignKey("experiment_plans_scientist_id_fkey", scientistId, Scientists)(_.id)
    val idx1 = index("experiment_plans_scientist_id_idx", scientistId)
  }

  lazy val ExperimentPlans = new TableQuery(tag => new ExperimentPlans(tag))

  case class Experiment(id: Int, notes: String, experimentPlanId: Int, createdAt: java.sql.Timestamp)

  class Experiments(_tableTag: Tag) extends Table[Experiment](_tableTag, "experiments") {
    def * = (id, notes, experimentPlanId, createdAt) <> (Experiment.tupled, Experiment.unapply)

    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    val notes: Rep[String] = column[String]("notes")
    val experimentPlanId: Rep[Int] = column[Int]("experiment_plan_id")
    val createdAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created_at")

    lazy val fk1 = foreignKey("experiments_experiment_plan_id_fkey", experimentPlanId, ExperimentPlans)(_.id)
    val idx1 = index("experiments_experiment_plan_id_idx", experimentPlanId)
  }

  lazy val Experiments = new TableQuery(tag => new Experiments(tag))

  case class ParticleDomainRow(id: Long, domainData: String, createdAt: java.sql.Timestamp)

  class ParticleDomain(_tableTag: Tag) extends Table[ParticleDomainRow](_tableTag, "particle_domain") {
    def * = (id, domainData, createdAt) <> (ParticleDomainRow.tupled, ParticleDomainRow.unapply)

    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    val domainData: Rep[String] = column[String]("data")
    val createdAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created_at")
  }

  lazy val ParticleDomain = new TableQuery(tag => new ParticleDomain(tag))

  case class QuantumDomainRow(id: Long, domainData: String, createdAt: java.sql.Timestamp)

  class QuantumDomain(_tableTag: Tag) extends Table[QuantumDomainRow](_tableTag, "quantum_domain") {
    def * = (id, domainData, createdAt) <> (QuantumDomainRow.tupled, QuantumDomainRow.unapply)

    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    val domainData: Rep[String] = column[String]("data")
    val createdAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created_at")
  }

  lazy val QuantumDomain = new TableQuery(tag => new QuantumDomain(tag))

  case class GravitationalWaveDomainRow(id: Long, domainData: String, createdAt: java.sql.Timestamp)

  class GravitationalWaveDomain(_tableTag: Tag) extends Table[GravitationalWaveDomainRow](_tableTag, "gravitational_wave_domain") {
    def * = (id, domainData, createdAt) <> (GravitationalWaveDomainRow.tupled, GravitationalWaveDomainRow.unapply)

    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    val domainData: Rep[String] = column[String]("data")
    val createdAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created_at")
  }

  lazy val GravitationalWaveDomain = new TableQuery(tag => new GravitationalWaveDomain(tag))

  case class ParticleColliderDataRow(id: Long, experimentId: Int, particleDomainId: Long, data1: Double, data2: Double, data3: Double, data4: Double, data5: Double, data6: Long, data7: Long, data8: Long, data9: String, data10: String, data11: String, createdAt: java.sql.Timestamp)

  class ParticleColliderData(_tableTag: Tag) extends Table[ParticleColliderDataRow](_tableTag, "particle_collider_data") {
    def * = (id, experimentId, particleDomainId, data1, data2, data3, data4, data5, data6, data7, data8, data9, data10, data11, createdAt) <> (ParticleColliderDataRow.tupled, ParticleColliderDataRow.unapply)

    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    val experimentId: Rep[Int] = column[Int]("experiment_id")
    val particleDomainId: Rep[Long] = column[Long]("particle_domain_data_id")
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

    lazy val fk1 = foreignKey("particle_collider_data_experiment_id_fkey", experimentId, Experiments)(_.id)
    lazy val fk2 = foreignKey("particle_collider_data_particle_domain_id_fkey", particleDomainId, ParticleDomain)(_.id)
    val idx1 = index("particle_collider_data_experiment_id_idx", experimentId)
    val idx2 = index("particle_collider_data_particle_domain_id_idx", particleDomainId)
  }

  lazy val ParticleColliderData = new TableQuery(tag => new ParticleColliderData(tag))

  case class QuantumDataRow(id: Long, experimentId: Int, quantumDomainId: Long, data1: String, data2: String, data3: String, createdAt: java.sql.Timestamp)

  class QuantumData(_tableTag: Tag) extends Table[QuantumDataRow](_tableTag, "quantum_data") {
    def * = (id, experimentId, quantumDomainId, data1, data2, data3, createdAt) <> (QuantumDataRow.tupled, QuantumDataRow.unapply)

    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    val experimentId: Rep[Int] = column[Int]("experiment_id")
    val quantumDomainId: Rep[Long] = column[Long]("quantum_domain_data_id")
    val data1: Rep[String] = column[String]("data_1")
    val data2: Rep[String] = column[String]("data_2")
    val data3: Rep[String] = column[String]("data_3")
    val createdAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created_at")

    lazy val fk1 = foreignKey("quantum_data_experiment_id_fkey", experimentId, Experiments)(_.id)
    lazy val fk2 = foreignKey("quantum_data_quantum_domain_id_fkey", quantumDomainId, QuantumDomain)(_.id)
    val idx1 = index("quantum_data_experiment_id_idx", experimentId)
    val idx2 = index("quantum_data_quantum_domain_id_idx", quantumDomainId)
  }

  lazy val QuantumData = new TableQuery(tag => new QuantumData(tag))

  case class GravitationalWaveDataRow(id: Long, experimentId: Int, gravitationalWaveDomainId: Long, data: String, createdAt: java.sql.Timestamp)

  class GravitationalWaveData(_tableTag: Tag) extends Table[GravitationalWaveDataRow](_tableTag, "gravitational_wave_data") {
    def * = (id, experimentId, gravitationalWaveDomainId, data, createdAt) <> (GravitationalWaveDataRow.tupled, GravitationalWaveDataRow.unapply)

    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    val experimentId: Rep[Int] = column[Int]("experiment_id")
    val gravitationalWaveDomainId: Rep[Long] = column[Long]("quantum_domain_data_id")
    val data: Rep[String] = column[String]("data")
    val createdAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created_at")

    lazy val fk1 = foreignKey("gravitational_wave_data_experiment_id_fkey", experimentId, Experiments)(_.id)
    lazy val fk2 = foreignKey("gravitational_wave_data_gravitational_wave_domain_id_fkey", gravitationalWaveDomainId, GravitationalWaveDomain)(_.id)
    val idx1 = index("gravitional_wave_data_experiment_id_idx", experimentId)
    val idx2 = index("gravitational_wave_data_gravitational_wave_domain_id_idx", gravitationalWaveDomainId)
  }

  lazy val GravitationalWaveData = new TableQuery(tag => new GravitationalWaveData(tag))

  case class DatumNote(id: Long, particleColliderDatumId: Option[Long], quantumDatumId: Option[Long], gravitationalWaveDatumId: Option[Long], note: String, createdAt: java.sql.Timestamp)

  class DatumNotes(_tableTag: Tag) extends Table[DatumNote](_tableTag, "datum_notes") {
    def * = (id, particleColliderDatumId, quantumDatumId, gravitationalWaveDatumId, note, createdAt) <> (DatumNote.tupled, DatumNote.unapply)

    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    val particleColliderDatumId: Rep[Option[Long]] = column[Option[Long]]("particle_collider_datum_id")
    val quantumDatumId: Rep[Option[Long]] = column[Option[Long]]("quantum_datum_id")
    val gravitationalWaveDatumId: Rep[Option[Long]] = column[Option[Long]]("gravitational_wave_datum_id")
    val note: Rep[String] = column[String]("note")
    val createdAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created_at")

    lazy val fk1 = foreignKey("datum_notes_particle_collider_datum_id_fkey", particleColliderDatumId, ParticleColliderData)(_.id.?)
    lazy val fk2 = foreignKey("datum_notes_quantum_datum_id_fkey", quantumDatumId, QuantumData)(_.id.?)
    lazy val fk3 = foreignKey("datum_notes_gravitational_wave_datum_id_fkey", gravitationalWaveDatumId, GravitationalWaveData)(_.id.?)
    val idx1 = index("datum_notes_particle_collider_datum_id_idx", particleColliderDatumId)
    val idx2 = index("datum_notes_quantum_datum_id_idx", quantumDatumId)
    val idx3 = index("datum_notes_gravitational_wave_datum_id_idx", gravitationalWaveDatumId)
  }

  lazy val DatumNotes = new TableQuery(tag => new DatumNotes(tag))

  case class DatumNoteResponse(id: Long, responseToDatumNoteId: Long, response: String, createdAt: java.sql.Timestamp)

  class DatumNoteResponses(_tableTag: Tag) extends Table[DatumNoteResponse](_tableTag, "datum_note_responses") {
    def * = (id, responseToDatumNoteId, response, createdAt) <> (DatumNoteResponse.tupled, DatumNoteResponse.unapply)

    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    val responseToDatumNoteId: Rep[Long] = column[Long]("response_to_datum_note_id")
    val response: Rep[String] = column[String]("response")
    val createdAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created_at")

    lazy val fk1 = foreignKey("datum_note_responses_response_to_datum_note_id_fkey", responseToDatumNoteId, DatumNotes)(_.id)
    val idx1 = index("datum_note_responses_response_to_datum_note_id_idx", responseToDatumNoteId)
  }

  lazy val DatumNoteResponses = new TableQuery(tag => new DatumNoteResponses(tag))
}
