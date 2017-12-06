package e2e.schooldb

import java.sql.{Date, Timestamp}

import slick.jdbc.JdbcProfile

class SchoolDBDML(val profile: JdbcProfile) extends SchoolDbDDL {

  import profile.api._

  def dbioSeq = {
    slick.dbio.DBIO.seq(
      Districts ++= (0 to 100).map { i =>
        DistrictsRow(
          i,
          s"District # $i",
          Timestamp.valueOf("2017-11-20 11:19:27.054177"),
          Timestamp.valueOf("2018-12-15 14:19:25.954172")
        )
      },
      Schools ++= (0 to 1000).map { i =>
        SchoolsRow(
          i % 99 + 1,
          s"School # $i",
          Some(s"Mascot# $i"),
          i,
          Timestamp.valueOf("2013-10-20 10:19:27.235677"),
          Timestamp.valueOf("2015-10-29 11:14:22.275870")
        )
      },
      Students ++= (0 to 1000000).map { i =>
        StudentsRow(
          i,
          s"Student # $i",
          Some(Date.valueOf("1950-01-01")),
          if (i % 5 == 0) Some((i % 9999) + 1) else None,
          Timestamp.valueOf("1999-10-22 11:12:22.354179"),
          Timestamp.valueOf("2001-10-23 08:09:21.435177")
        )
      },
      SchoolAssignments ++= (0 to 1000000).filterNot(i => i % 10 == 0 || i % 5 == 0).map { i =>
        SchoolAssignmentsRow(
          (i % 9999) + 1,
          i,
          Date.valueOf("1900-01-01"),
          Some(Date.valueOf("1904-01-01")),
          Timestamp.valueOf("2018-11-20 11:19:27.054171"),
          Timestamp.valueOf("2019-12-15 14:19:25.954171")
        )
      },
      SchoolAssignments ++= (0 to 1000000).filter(_ % 5 == 0).map { i =>
        SchoolAssignmentsRow(
          (i % 9999) + 1,
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
      ),
      HomeworkGrades ++= (0 to 2000000).map { i =>
        HomeworkGradesRow(
          i,
          i / 1000000,
          if (i % 3 == 0) "worksheet" else if (i % 5 == 0) "essay" else "multiple choice", // Pick an assignment id from 1 to 4
          (i % 4) + 1, // Pick an assignment id from 1 to 4,
          Some((i % 100) + ((i % 50).toDouble / (i % 50 + 50).toDouble)),
          if (i % 3 == 0) Some(true) else Some(false),
          Timestamp.valueOf("2015-11-25 08:19:27.333665"),
          Timestamp.valueOf("2017-11-25 09:19:27.333667")
        )
      },
      EventTypes ++= Seq(
        EventTypesRow("enrollment"),
        EventTypesRow("standardized_testing"),
        EventTypesRow("graduation"),
        EventTypesRow("student_class_attendance")
      ),
      Events ++= (0 to 1000000).map { i =>
        EventsRow(
          i,
          "enrollment"
        )
      }
    )
  }
}

//
//(0 to 1000).filterNot(_ % 10 == 7).map { i =>
//Schools.filter(_.id === i).map(_.latestValedictorianIdCache).update(Some(i * 1000))
//},

//
//    -- Insert some enrollment events
//    INSERT INTO "Audit".events (event_type_key, district_id, school_id, student_id, school_assignment_school_id, school_assignment_student_id, created_at)
//    SELECT
//    'enrollment',
//    sc.district_id,
//    sc.id,
//    sa.student_id,
//    sa.school_id,
//    sa.student_id,
//    '2005-10-24 11:19:27.888888'
//    FROM school_assignments sa
//    INNER JOIN schools sc ON sa.school_id = sc.id;
//
//    -- Insert some enrollment events
//    INSERT INTO "Audit".events (event_type_key, district_id, school_id, created_at)
//    SELECT
//    'standardized_testing',
//    sc.district_id,
//    sc.id,
//    '2010-10-25 11:19:27.333666'
//    FROM schools sc;
//
//    -- Insert some graduation events
//    INSERT INTO "Audit".events (event_type_key, district_id, school_id, student_id, created_at)
//    SELECT
//    'graduation',
//    sc.district_id,
//    sc.id,
//    sa.student_id,
//    sa.assignment_end
//    FROM school_assignments sa
//    INNER JOIN schools sc ON sa.school_id = sc.id
//    WHERE sa.assignment_end IS NOT NULL;
//
//    -- Insert a large number of events representing "the fact that a student attended a particular class on a particular day"
//    -- This is meant to be an example of a 1 to n relationship with a very high cardinality
//      INSERT INTO "Audit".events (event_type_key, district_id, school_id, student_id, school_assignment_school_id, school_assignment_student_id, created_at)
//    SELECT
//    'student_class_attendance',
//    sc.district_id,
//    sc.id,
//    sa.student_id,
//    sa.school_id,
//    sa.student_id,
//    '1999-10-25 11:19:27.888999'
//    FROM school_assignments sa
//    INNER JOIN schools sc ON sa.school_id = sc.id
//    CROSS JOIN generate_series(0, 3) AS seq; -- (0, 2000) would generate ~ 200 GB of data once indices are created

