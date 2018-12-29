package load.physics

import java.sql.Timestamp

class PhysicsDML(ddl: PhysicsDDL) {
  import ddl._
  import ddl.profile.api._

  private val numResearchInstitutions = 10
  private val numResearchGroups = 30
  private val numScientists = 100
  private val numExperimentTypes = 10
  private val numExperiments = 200
  val numParticleColliderData = numExperiments * 25000
  val numQuantumData = numExperiments * 30000
  val numGravitationalWaveData = numExperiments * 20000
  val particleColliderNotesFactor = 13
  val quantumNotesFactor = 11
  val gravitationalWaveNotesFactor = 15

  def initialInserts = {
    val seq = Seq(
      ResearchInstitutions ++= (1 to numResearchInstitutions).map { i =>
        ResearchInstitution(
          i,
          s"Research Institution # $i",
          Timestamp.valueOf("1980-11-20 11:19:27.054177")
        )
      },
      ResearchGroups ++= (1 to numResearchGroups).map { i =>
        ResearchGroup(
          i,
          s"Research Group # $i",
          i % (numResearchInstitutions - 1) + 1,
          Timestamp.valueOf("1990-10-29 11:11:23.275470")
        )
      },
      Scientists ++= (1 to numScientists).map { i =>
        Scientist(
          i,
          s"Scientist # $i",
          if (i % 20 == 0) None else Some((i % (numResearchGroups - 1)) + 1),
          Timestamp.valueOf("2001-10-23 08:09:21.435177")
        )
      },
      ExperimentMetadata ++= (1 to numExperimentTypes).map { i =>
        ExperimentMetadataRow(
          i,
          s"Experiment Metadata # $i",
          (i % (numScientists - 1)) + 1,
          Timestamp.valueOf("2002-12-15 14:19:25.954171")
        )
      },
      ExperimentPlans ++= (1 to numExperimentTypes).map { i =>
        ExperimentPlan(
          i,
          s"Experiment Plan Description # $i",
          (i % (numScientists - 1)) + 1,
          i,
          Timestamp.valueOf("2001-09-24 11:19:27.999999")
        )
      },
      Experiments ++= (1 to numExperiments).map { i =>
        Experiment(
          i,
          s"Experiment Notes # $i",
          (i % (numExperimentTypes - 1)) + 1,
          Timestamp.valueOf("2005-12-15 14:19:25.954171")
        )
      }
    )

    DBIO.seq(seq: _*)
  }

  def particleDomainInserts = {
    (1 to 1000000).grouped(10000).toVector.map { xs =>
      ParticleDomain ++= xs.map { i =>
        ParticleDomainRow(
          i,
          s"Particle Domain Row # $i",
          Timestamp.valueOf("1982-12-15 14:19:25.954171")
        )
      }
    }
  }

  def quantumDomainInserts = {
    (1 to 1000000).grouped(10000).toVector.map { xs =>
      QuantumDomain ++= xs.map { i =>
        QuantumDomainRow(
          i,
          s"Quantum Domain Row # $i",
          Timestamp.valueOf("1983-07-26 18:19:25.253169")
        )
      }
    }
  }

  def gravitationalWaveDomainInserts = {
    (1 to 1000000).grouped(10000).toVector.map { xs =>
      GravitationalWaveDomain ++= xs.map { i =>
        GravitationalWaveDomainRow(
          i,
          s"Gravitational Wave Domain Row # $i",
          Timestamp.valueOf("2001-12-29 23:59:59.182131")
        )
      }
    }
  }

  def particleColliderDataInserts = {
    val factor = 25000
    (1 to (numExperiments * factor)).grouped(5000).toVector.map { xs =>
      ParticleColliderData ++= xs.map { i =>
        ParticleColliderDataRow(
          i,
          (((i + (factor - 1)) / factor) % (numExperiments - 1)) + 1,
          (i % (1000000 - 1)) + 1,
          i,
          (i * 2) * 1.378d,
          (i * 3) * 1.378d,
          (i * 4) * 1.378d,
          (i * 5) * 1.378d,
          i * 6,
          i * 7,
          i * 8,
          s"Particle Collider Data # $i (1)",
          s"More Particle Collider Data # $i (2)",
          s"Even Particle Collider Data # $i (3)",
          Timestamp.valueOf("2001-12-29 23:59:59.182131")
        )
      }
    }
  }

  def quantumDataInserts = {
    val factor = 30000
    (1 to (numExperiments * factor)).grouped(5000).toVector.map { xs =>
      QuantumData ++= xs.map { i =>
        QuantumDataRow(
          i,
          (((i + (factor - 1)) / factor) % (numExperiments - 1)) + 1,
          (i % (1000000 - 1)) + 1,
          s"Quantum Data # $i (1)",
          s"More Quantum Data # $i (2)",
          s"Even More Quantum Data # $i (3)",
          Timestamp.valueOf("2001-12-29 23:59:59.182131")
        )
      }
    }
  }

  def gravitationalWaveDataInserts = {
    val factor = 20000
    (1 to (numExperiments * factor)).grouped(5000).toVector.map { xs =>
      GravitationalWaveData ++= xs.map { i =>
        GravitationalWaveDataRow(
          i,
          (((i + (factor - 1)) / factor) % (numExperiments - 1)) + 1,
          (i % (1000000 - 1)) + 1,
          s"Quantum Data # $i (1)",
          Timestamp.valueOf("2001-12-29 23:59:59.182131")
        )
      }
    }
  }

  def particleColliderNotesInserts(parentId: Long) = {
    val factor = particleColliderNotesFactor
    DatumNotes ++= (1 to factor).map { i =>
      val pk = i + ((parentId - 1) * factor)
      DatumNote(
        pk,
        Some((pk % (numParticleColliderData - 1)) + 1),
        None,
        None,
        s"$pk This $pk is $pk a $pk particle $pk collider $pk datum $pk note $pk: ${pk.toString * 1000}",
        Timestamp.valueOf("2010-12-31 14:32:59.283134")
      )
    }
  }

  def quantumNotesInserts(parentId: Long) = {
    val factor = quantumNotesFactor
    val startingPk = numParticleColliderData * particleColliderNotesFactor + 1
    DatumNotes ++= (1 to factor).map { i =>
      val pk = startingPk + i + ((parentId - 1) * factor)
      DatumNote(
        pk,
        None,
        Some((pk % (numQuantumData - 1)) + 1),
        None,
        s"$pk This $pk is $pk a $pk quantum $pk datum $pk note $pk: ${pk.toString * 500}",
        Timestamp.valueOf("2011-06-31 14:18:23.783834")
      )
    }

  }

  def gravitationWaveNotesInserts(parentId: Long) = {
    val factor = gravitationalWaveNotesFactor
    val startingPk = (numParticleColliderData * particleColliderNotesFactor) + (numQuantumData * quantumNotesFactor) + 1
    DatumNotes ++= (1 to factor).map { i =>
      val pk = startingPk + i + ((parentId - 1) * factor)
      DatumNote(
        pk,
        None,
        None,
        Some((pk % (numGravitationalWaveData - 1)) + 1),
        s"$pk This $pk is $pk a $pk gravitational $pk wave $pk datum $pk note $pk: ${pk.toString * 200}",
        Timestamp.valueOf("1987-02-01 00:00:01.723434")
      )
    }
  }
}


