package load.physics

import java.sql.Timestamp

import slick.jdbc.JdbcProfile

class PhysicsDML(val profile: JdbcProfile) extends PhysicsDDL {

  import profile.api._

  private val numResearchInstitutions = 10
  private val numResearchGroups = 30
  private val numScientists = 100
  private val numExperimentTypes = 10
  private val numExperiments = 200

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
}


