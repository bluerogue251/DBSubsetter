package load.schooldb

import java.sql.{Date, Timestamp}

import slick.jdbc.JdbcProfile

class SchoolDBDML(val profile: JdbcProfile) extends SchoolDbDDL {

  import profile.api._

  private val numDistricts = 100
  private val numSchools = 10000
  private val numStudents = 1000000
  private val noSchool = 21

  def initialInserts = {
    val seq = Seq(
      Districts ++= (1 to numDistricts).map { i =>
        DistrictsRow(
          i,
          s"District # $i",
          Timestamp.valueOf("2017-11-20 11:19:27.054177"),
          Timestamp.valueOf("2018-12-15 14:19:25.954172")
        )
      },
      Schools ++= (1 to numSchools).map { i =>
        SchoolsRow(
          i % (numDistricts - 1) + 1,
          s"School # $i",
          Some(s"Mascot# $i"),
          i,
          Timestamp.valueOf("2013-10-20 10:19:27.235677"),
          Timestamp.valueOf("2015-10-29 11:14:22.275870")
        )
      },
      Students ++= (1 to numStudents).map { i =>
        StudentsRow(
          i,
          s"Student # $i",
          Some(Date.valueOf("1950-01-01")),
          if (i % 5 == 0) Some((i % (numSchools - 1)) + 1) else None,
          Timestamp.valueOf("1999-10-22 11:12:22.354179"),
          Timestamp.valueOf("2001-10-23 08:09:21.435177")
        )
      },
      SchoolAssignments ++= (1 to numStudents).filterNot(_ % noSchool == 0).filterNot(_ % 5 == 0).map { i =>
        SchoolAssignmentsRow(
          (i % (numSchools - 1)) + 1,
          i,
          Date.valueOf("1900-01-01"),
          Some(Date.valueOf("1904-01-01")),
          Timestamp.valueOf("2018-11-20 11:19:27.054171"),
          Timestamp.valueOf("2019-12-15 14:19:25.954171")
        )
      },
      SchoolAssignments ++= (1 to numStudents).filterNot(_ % noSchool == 0).filter(_ % 5 == 0).map { i =>
        SchoolAssignmentsRow(
          (i % (numSchools - 1)) + 1,
          i,
          Date.valueOf("1900-01-01"),
          None,
          Timestamp.valueOf("1890-10-24 11:19:27.054177"),
          Timestamp.valueOf("1895-10-24 11:19:27.999999")
        )
      },
      StandaloneTable ++= Seq(
        StandaloneTableRow(1, Some("Note # 1"), Some(Date.valueOf("1990-01-01"))),
        StandaloneTableRow(2, Some("Note # 2"), Some(Date.valueOf("1991-01-01"))),
        StandaloneTableRow(3, Some("Note # 3"), Some(Date.valueOf("1992-01-01"))),
        StandaloneTableRow(4, Some("Note # 4"), Some(Date.valueOf("1993-01-01")))
      ),
      MultipleChoiceAssignments ++= Seq(
        MultipleChoiceAssignmentsRow(1, "Biology 1 Midterm - Take Home", Timestamp.valueOf("1950-01-01 00:00:00.000000")),
        MultipleChoiceAssignmentsRow(2, "Biology 1 Final - Take Home", Timestamp.valueOf("1950-01-02 00:00:00.000000")),
        MultipleChoiceAssignmentsRow(3, "Biology 2 Midterm - Take Home", Timestamp.valueOf("1950-01-03 00:00:00.000000")),
        MultipleChoiceAssignmentsRow(4, "Biology 2 Final Exam - Take Home", Timestamp.valueOf("1950-01-04 00:00:00.000000"))
      ),
      WorksheetAssignments ++= Seq(
        WorksheetAssignmentsRow(1, "Chemistry 1 Midterm - Take Home"),
        WorksheetAssignmentsRow(2, "Chemistry 1 Final - Take Home"),
        WorksheetAssignmentsRow(3, "Chemistry 2 Midterm - Take Home"),
        WorksheetAssignmentsRow(4, "Chemistry 2 Final - Take Home")
      ),
      EssayAssignments ++= Seq(
        EssayAssignmentsRow(1, "Persuasive Writing Assignment"),
        EssayAssignmentsRow(2, "Personal Narrative Assignment"),
        EssayAssignmentsRow(3, "Journalism Midterm"),
        EssayAssignmentsRow(4, "Journalism Final"),
        EssayAssignmentsRow(5, "Biographical Writing Assignment")
      )
    )
    DBIO.seq(seq: _*)
  }

  def homeworkGradeInserts = {
    val factor = 3
    val seq = Seq(
      HomeworkGrades ++= (1 to (numStudents * factor)).map { i =>
        HomeworkGradesRow(
          i,
          (i + factor - 1) / factor,
          if (i % 3 == 0) "worksheet" else if (i % 5 == 0) "essay" else "multiple choice", // Pick an assignment id from 1 to 4
          (i % 4) + 1, // Pick an assignment id from 1 to 4,
          Some((i % 100) + ((i % 50).toDouble / (i % 50 + 50).toDouble)),
          if (i % 3 == 0) Some(true) else Some(false),
          Timestamp.valueOf("2015-11-25 08:19:27.333665"),
          Timestamp.valueOf("2017-11-25 09:19:27.333667")
        )
      }
    )
    DBIO.seq(seq: _*)
  }

  def eventInserts1 = {
    val seq = Seq(
      EventTypes ++= Seq(
        EventTypesRow("enrollment"),
        EventTypesRow("standardized_testing"),
        EventTypesRow("graduation"),
        EventTypesRow("student_class_attendance")
      ),
      Events ++= (1 to numStudents).filterNot(_ % noSchool == 0).map { i =>
        EventsRow(
          i,
          "enrollment",
          Some((i % (numDistricts - 1)) + 1),
          Some((i % (numSchools - 1)) + 1),
          Some(i),
          Some((i % (numSchools - 1)) + 1),
          Some(i),
          None,
          None,
          None,
          None,
          None,
          Timestamp.valueOf("2005-10-24 11:19:27.888888")
        )
      }
    )
    DBIO.seq(seq: _*)
  }

  def eventsInsert2 = {
    val seq = Seq(
      Events ++= (1 to numSchools).map { i =>
        EventsRow(
          i + 1000000,
          "standardized_testing",
          Some(i % (numDistricts - 1) + 1),
          Some((i - 1 + (numSchools / numDistricts)) / (numSchools / numDistricts)),
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          Timestamp.valueOf("2010-10-25 11:19:27.333666")
        )
      }
    )
    DBIO.seq(seq: _*)
  }

  def eventsInsert3 = {
    val factor = 4
    val seq = Seq(
      Events ++= (1 to numStudents * factor).filterNot(i => ((i + 2) / factor) % noSchool == 0).map { i =>
        EventsRow(
          i + 2000000,
          "student_class_attendance",
          Some((((i + 2) / factor) % (numDistricts - 1)) + 1),
          Some((((i + 2) / factor) % (numSchools - 1)) + 1),
          Some((i + 2) / factor),
          Some((((i + 2) / factor) % (numSchools - 1)) + 1),
          Some((i + 2) / factor),
          None,
          None,
          None,
          None,
          None,
          Timestamp.valueOf("1999-10-25 11:19:27.888999")
        )
      }
    )
    DBIO.seq(seq: _*)
  }

  def latestValedictorianCacheUpdates = {
    val updates = (1 until numSchools).filterNot(_ % 10 == 7).map { i =>
      Schools.filter(_.id === i).map(_.latestValedictorianIdCache).update(Some((i * (numStudents / numSchools)) + (i % 101)))
    }
    DBIO.seq(updates: _*)
  }
}


