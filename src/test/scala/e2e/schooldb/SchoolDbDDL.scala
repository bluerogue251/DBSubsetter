package e2e.schooldb

trait SchoolDbDDL {
  val profile: slick.jdbc.JdbcProfile

  import profile.api._

  lazy val schema: profile.SchemaDescription = Array(Districts.schema, EmptyTable1.schema, EmptyTable2.schema, EmptyTable3.schema, EmptyTable4.schema, EmptyTable5.schema, EssayAssignments.schema, Events.schema, EventTypes.schema, HomeworkGrades.schema, MultipleChoiceAssignments.schema, SchoolAssignments.schema, Schools.schema, StandaloneTable.schema, Students.schema, WorksheetAssignments.schema).reduceLeft(_ ++ _)

  case class DistrictsRow(id: Int, name: String, createdAt: java.sql.Timestamp, updatedAt: java.sql.Timestamp)

  class Districts(_tableTag: Tag) extends profile.api.Table[DistrictsRow](_tableTag, "districts") {
    def * = (id, name, createdAt, updatedAt) <> (DistrictsRow.tupled, DistrictsRow.unapply)

    val id: Rep[Int] = column[Int]("Id", O.AutoInc, O.PrimaryKey)
    val name: Rep[String] = column[String]("name")
    val createdAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created_at")
    val updatedAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("updated_at")
  }

  lazy val Districts = new TableQuery(tag => new Districts(tag))


  case class EmptyTable1Row(id: Int)

  class EmptyTable1(_tableTag: Tag) extends profile.api.Table[EmptyTable1Row](_tableTag, "empty_table_1") {
    def * = id <> (EmptyTable1Row, EmptyTable1Row.unapply)

    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
  }

  lazy val EmptyTable1 = new TableQuery(tag => new EmptyTable1(tag))


  case class EmptyTable2Row(id: Int)

  class EmptyTable2(_tableTag: Tag) extends profile.api.Table[EmptyTable2Row](_tableTag, "empty_table_2") {
    def * = id <> (EmptyTable2Row, EmptyTable2Row.unapply)

    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
  }

  lazy val EmptyTable2 = new TableQuery(tag => new EmptyTable2(tag))

  case class EmptyTable3Row(id: Int)

  class EmptyTable3(_tableTag: Tag) extends profile.api.Table[EmptyTable3Row](_tableTag, "empty_table_3") {
    def * = id <> (EmptyTable3Row, EmptyTable3Row.unapply)

    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
  }

  lazy val EmptyTable3 = new TableQuery(tag => new EmptyTable3(tag))

  case class EmptyTable4Row(id: Int)

  class EmptyTable4(_tableTag: Tag) extends profile.api.Table[EmptyTable4Row](_tableTag, "empty_table_4") {
    def * = id <> (EmptyTable4Row, EmptyTable4Row.unapply)

    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
  }

  lazy val EmptyTable4 = new TableQuery(tag => new EmptyTable4(tag))

  case class EmptyTable5Row(id: Int)

  class EmptyTable5(_tableTag: Tag) extends profile.api.Table[EmptyTable5Row](_tableTag, "empty_table_5") {
    def * = id <> (EmptyTable5Row, EmptyTable5Row.unapply)

    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
  }

  lazy val EmptyTable5 = new TableQuery(tag => new EmptyTable5(tag))

  case class EssayAssignmentsRow(id: Int, name: String)

  class EssayAssignments(_tableTag: Tag) extends profile.api.Table[EssayAssignmentsRow](_tableTag, "essay_assignments") {
    def * = (id, name) <> (EssayAssignmentsRow.tupled, EssayAssignmentsRow.unapply)

    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    val name: Rep[String] = column[String]("name")
  }

  lazy val EssayAssignments = new TableQuery(tag => new EssayAssignments(tag))

  case class EventsRow(id: Long, eventTypeKey: String, districtId: Option[Int] = None, schoolId: Option[Int] = None, studentId: Option[Long] = None, schoolAssignmentSchoolId: Option[Int] = None, schoolAssignmentStudentId: Option[Long] = None, emptyTable1Id: Option[Int] = None, emptyTable2Id: Option[Int] = None, emptyTable3Id: Option[Int] = None, emptyTable4Id: Option[Int] = None, emptyTable5Id: Option[Int] = None, createdAt: java.sql.Timestamp)

  class Events(_tableTag: Tag) extends profile.api.Table[EventsRow](_tableTag, Some("Audit"), "events") {
    def * = (id, eventTypeKey, districtId, schoolId, studentId, schoolAssignmentSchoolId, schoolAssignmentStudentId, emptyTable1Id, emptyTable2Id, emptyTable3Id, emptyTable4Id, emptyTable5Id, createdAt) <> (EventsRow.tupled, EventsRow.unapply)

    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    val eventTypeKey: Rep[String] = column[String]("event_type_key", O.Length(30, varying = true))
    val districtId: Rep[Option[Int]] = column[Option[Int]]("district_id", O.Default(None))
    val schoolId: Rep[Option[Int]] = column[Option[Int]]("school_id", O.Default(None))
    val studentId: Rep[Option[Long]] = column[Option[Long]]("student_id", O.Default(None))
    val schoolAssignmentSchoolId: Rep[Option[Int]] = column[Option[Int]]("school_assignment_school_id", O.Default(None))
    val schoolAssignmentStudentId: Rep[Option[Long]] = column[Option[Long]]("school_assignment_student_id", O.Default(None))
    val emptyTable1Id: Rep[Option[Int]] = column[Option[Int]]("empty_table_1_id", O.Default(None))
    val emptyTable2Id: Rep[Option[Int]] = column[Option[Int]]("empty_table_2_id", O.Default(None))
    val emptyTable3Id: Rep[Option[Int]] = column[Option[Int]]("empty_table_3_id", O.Default(None))
    val emptyTable4Id: Rep[Option[Int]] = column[Option[Int]]("empty_table_4_id", O.Default(None))
    val emptyTable5Id: Rep[Option[Int]] = column[Option[Int]]("empty_table_5_id", O.Default(None))
    val createdAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created_at")

    lazy val eventTypesFk = foreignKey("events_event_type_key_fkey", eventTypeKey, EventTypes)(r => r.key, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
    lazy val districtsFk = foreignKey("events_district_id_fkey", districtId, Districts)(r => Rep.Some(r.id), onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
    lazy val schoolsFk = foreignKey("events_school_id_fkey", schoolId, Schools)(r => Rep.Some(r.id), onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
    lazy val studentsFk = foreignKey("events_student_id_fkey", studentId, Students)(r => Rep.Some(r.studentId), onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
    lazy val schoolAssignmentsFk = foreignKey("events_school_assignment_school_id_fkey", (schoolAssignmentSchoolId, schoolAssignmentStudentId), SchoolAssignments)(r => (Rep.Some(r.schoolId), Rep.Some(r.studentId)), onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
    lazy val emptyTable1Fk = foreignKey("events_empty_table_1_id_fkey", emptyTable1Id, EmptyTable1)(r => Rep.Some(r.id), onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
    lazy val emptyTable2Fk = foreignKey("events_empty_table_2_id_fkey", emptyTable2Id, EmptyTable2)(r => Rep.Some(r.id), onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
    lazy val emptyTable3Fk = foreignKey("events_empty_table_3_id_fkey", emptyTable3Id, EmptyTable3)(r => Rep.Some(r.id), onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
    lazy val emptyTable4Fk = foreignKey("events_empty_table_4_id_fkey", emptyTable4Id, EmptyTable4)(r => Rep.Some(r.id), onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
    lazy val emptyTable5Fk = foreignKey("events_empty_table_5_id_fkey", emptyTable5Id, EmptyTable5)(r => Rep.Some(r.id), onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)

    val index1 = index("events_event_type_key_idx", eventTypeKey)
    val index2 = index("events_event_district_id_idx", districtId)
    val index3 = index("events_event_school_id_idx", schoolId)
    val index4 = index("events_event_student_id_idx", studentId)
    val index5 = index("events_event_school_assignment_school_id_idx", schoolAssignmentSchoolId)
    val index6 = index("events_event_school_assignment_student_id_idx", schoolAssignmentStudentId)
    val index7 = index("events_event_empty_table_1_id_idx", emptyTable1Id)
    val index8 = index("events_event_empty_table_2_id_idx", emptyTable2Id)
    val index9 = index("events_event_empty_table_3_id_idx", emptyTable3Id)
    val index10 = index("events_event_empty_table_4_id_idx", emptyTable4Id)
    val index11 = index("events_event_empty_table_5_id_idx", emptyTable5Id)
  }

  lazy val Events = new TableQuery(tag => new Events(tag))

  case class EventTypesRow(key: String)

  class EventTypes(_tableTag: Tag) extends profile.api.Table[EventTypesRow](_tableTag, Some("Audit"), "event_types") {
    def * = key <> (EventTypesRow, EventTypesRow.unapply)

    val key: Rep[String] = column[String]("key", O.PrimaryKey, O.Length(30, varying = true))
  }

  lazy val EventTypes = new TableQuery(tag => new EventTypes(tag))

  case class HomeworkGradesRow(id: Long, studentId: Long, assignmentType: String, assignmentId: Int, grade: Option[scala.math.BigDecimal] = None, autograded: Option[Boolean] = None, createdAt: java.sql.Timestamp, updatedAt: java.sql.Timestamp)

  class HomeworkGrades(_tableTag: Tag) extends profile.api.Table[HomeworkGradesRow](_tableTag, "homework_grades") {
    def * = (id, studentId, assignmentType, assignmentId, grade, autograded, createdAt, updatedAt) <> (HomeworkGradesRow.tupled, HomeworkGradesRow.unapply)

    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    val studentId: Rep[Long] = column[Long]("student_id")
    val assignmentType: Rep[String] = column[String]("assignment_type")
    val assignmentId: Rep[Int] = column[Int]("assignment_id")
    val grade: Rep[Option[scala.math.BigDecimal]] = column[Option[scala.math.BigDecimal]]("grade", O.Default(None))
    val autograded: Rep[Option[Boolean]] = column[Option[Boolean]]("autograded", O.Default(None))
    val createdAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created_at")
    val updatedAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("updated_at")

    lazy val studentsFk = foreignKey("student_id", studentId, Students)(_.studentId, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
    val index1 = index("homework_grades_student_id_idx", studentId)
    val index2 = index("homework_grades_assignment_type_idx", assignmentType)
    val index3 = index("homework_grades_assignment_id_idx", assignmentId)
  }

  lazy val HomeworkGrades = new TableQuery(tag => new HomeworkGrades(tag))

  case class MultipleChoiceAssignmentsRow(id: Int, assignmentName: String, createdAt: java.sql.Timestamp)

  class MultipleChoiceAssignments(_tableTag: Tag) extends profile.api.Table[MultipleChoiceAssignmentsRow](_tableTag, "multiple_choice_assignments") {
    def * = (id, assignmentName, createdAt) <> (MultipleChoiceAssignmentsRow.tupled, MultipleChoiceAssignmentsRow.unapply)

    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    val assignmentName: Rep[String] = column[String]("assignment_name")
    val createdAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created_at")
  }

  lazy val MultipleChoiceAssignments = new TableQuery(tag => new MultipleChoiceAssignments(tag))

  case class SchoolAssignmentsRow(schoolId: Int, studentId: Long, assignmentStart: java.sql.Date, assignmentEnd: Option[java.sql.Date] = None, createdAt: java.sql.Timestamp, updatedAt: java.sql.Timestamp)

  class SchoolAssignments(_tableTag: Tag) extends profile.api.Table[SchoolAssignmentsRow](_tableTag, "school_assignments") {
    def * = (schoolId, studentId, assignmentStart, assignmentEnd, createdAt, updatedAt) <> (SchoolAssignmentsRow.tupled, SchoolAssignmentsRow.unapply)

    val schoolId: Rep[Int] = column[Int]("school_id")
    val studentId: Rep[Long] = column[Long]("student_id")
    val assignmentStart: Rep[java.sql.Date] = column[java.sql.Date]("assignment_start")
    val assignmentEnd: Rep[Option[java.sql.Date]] = column[Option[java.sql.Date]]("assignment_end", O.Default(None))
    val createdAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created_at")
    val updatedAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("updated_at")

    val pk = primaryKey("school_assignments_pkey", (schoolId, studentId))
    lazy val studentsFk = foreignKey("school_assignments_student_id_fkey", studentId, Students)(r => r.studentId, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
    lazy val schoolsFk = foreignKey("school_assignments_school_id_fkey", schoolId, Schools)(r => r.id, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
    val index1 = index("school_assignments_school_id_idx", schoolId)
    val index2 = index("school_assignments_student_id_idx", studentId)
  }

  lazy val SchoolAssignments = new TableQuery(tag => new SchoolAssignments(tag))

  case class SchoolsRow(districtId: Int, name: String, mascot: Option[String] = None, id: Int, createdAt: java.sql.Timestamp, updatedAt: java.sql.Timestamp, latestValedictorianIdCache: Option[Long] = None)

  class Schools(_tableTag: Tag) extends profile.api.Table[SchoolsRow](_tableTag, "schools") {
    def * = (districtId, name, mascot, id, createdAt, updatedAt, latestValedictorianIdCache) <> (SchoolsRow.tupled, SchoolsRow.unapply)

    val districtId: Rep[Int] = column[Int]("district_id")
    val name: Rep[String] = column[String]("name")
    val mascot: Rep[Option[String]] = column[Option[String]]("mascot", O.Default(None))
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    val createdAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created_at")
    val updatedAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("updated_at")
    val latestValedictorianIdCache: Rep[Option[Long]] = column[Option[Long]]("latest_valedictorian_id_cache", O.Default(None))

    lazy val districtsFk = foreignKey("schools_district_id_fkey", districtId, Districts)(_.id, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
    lazy val studentsFk = foreignKey("schools_latest_valedictorian_id_cache_fkey", latestValedictorianIdCache, Students)(r => r.studentId, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
    val index1 = index("schools_district_id_idx", districtId)
    val index2 = index("schools_latest_valedictorian_id_cache_idx", latestValedictorianIdCache)
  }

  lazy val Schools = new TableQuery(tag => new Schools(tag))

  case class StandaloneTableRow(id: Long, note: Option[String] = None, createdOn: Option[java.sql.Date] = None)

  class StandaloneTable(_tableTag: Tag) extends profile.api.Table[StandaloneTableRow](_tableTag, "standalone_table") {
    def * = (id, note, createdOn) <> (StandaloneTableRow.tupled, StandaloneTableRow.unapply)

    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    val note: Rep[Option[String]] = column[Option[String]]("note", O.Default(None))
    val createdOn: Rep[Option[java.sql.Date]] = column[Option[java.sql.Date]]("created_on", O.Default(None))
  }

  lazy val StandaloneTable = new TableQuery(tag => new StandaloneTable(tag))

  case class StudentsRow(studentId: Long, name: String, dateOfBirth: Option[java.sql.Date] = None, currentSchoolIdCache: Option[Int] = None, createdAt: java.sql.Timestamp, updatedAt: java.sql.Timestamp)
  class Students(_tableTag: Tag) extends profile.api.Table[StudentsRow](_tableTag, "Students") {
    def * = (studentId, name, dateOfBirth, currentSchoolIdCache, createdAt, updatedAt) <> (StudentsRow.tupled, StudentsRow.unapply)

    val studentId: Rep[Long] = column[Long]("student_id", O.AutoInc, O.PrimaryKey)
    val name: Rep[String] = column[String]("name")
    val dateOfBirth: Rep[Option[java.sql.Date]] = column[Option[java.sql.Date]]("date_of_birth", O.Default(None))
    val currentSchoolIdCache: Rep[Option[Int]] = column[Option[Int]]("current_school_id_cache", O.Default(None))
    val createdAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created_at")
    val updatedAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("updated_at")

    lazy val schoolFk = foreignKey("Students_current_school_id_cache_fkey", currentSchoolIdCache, Schools)(_.id, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
    val index2 = index("Students_current_school_id_cache_idx", currentSchoolIdCache)
  }

  lazy val Students = new TableQuery(tag => new Students(tag))

  case class WorksheetAssignmentsRow(id: Int, worksheetAssignmentName: String)

  class WorksheetAssignments(_tableTag: Tag) extends profile.api.Table[WorksheetAssignmentsRow](_tableTag, "worksheet_assignments") {
    def * = (id, worksheetAssignmentName) <> (WorksheetAssignmentsRow.tupled, WorksheetAssignmentsRow.unapply)

    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    val worksheetAssignmentName: Rep[String] = column[String]("worksheet_assignment_name")
  }

  lazy val WorksheetAssignments = new TableQuery(tag => new WorksheetAssignments(tag))
}

