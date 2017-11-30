/*! SET SESSION sql_mode='ANSI'; */

ALTER TABLE "Audit".events
  ADD PRIMARY KEY (id);

CREATE INDEX ON "Audit".events (event_type_key);
CREATE INDEX ON "Audit".events (district_id);
CREATE INDEX ON "Audit".events (school_id);
CREATE INDEX ON "Audit".events (student_id);
CREATE INDEX ON "Audit".events (school_assignment_school_id);
CREATE INDEX ON "Audit".events (school_assignment_student_id);
CREATE INDEX ON "Audit".events (empty_table_1_id);
CREATE INDEX ON "Audit".events (empty_table_2_id);
CREATE INDEX ON "Audit".events (empty_table_3_id);
CREATE INDEX ON "Audit".events (empty_table_4_id);
CREATE INDEX ON "Audit".events (empty_table_5_id);

ALTER TABLE "Audit".events
  ADD FOREIGN KEY (event_type_key) REFERENCES "Audit".event_types (key);
ALTER TABLE "Audit".events
  ADD FOREIGN KEY (district_id) REFERENCES districts ("Id");
ALTER TABLE "Audit".events
  ADD FOREIGN KEY (school_id) REFERENCES schools (id);
ALTER TABLE "Audit".events
  ADD FOREIGN KEY (student_id) REFERENCES "Students" (student_id);
-- Composite foreign key
ALTER TABLE "Audit".events
  ADD FOREIGN KEY (school_assignment_school_id, school_assignment_student_id) REFERENCES school_assignments (school_id, student_id);
ALTER TABLE "Audit".events
  ADD FOREIGN KEY (empty_table_1_id) REFERENCES empty_table_1 (id);
ALTER TABLE "Audit".events
  ADD FOREIGN KEY (empty_table_2_id) REFERENCES empty_table_2 (id);
ALTER TABLE "Audit".events
  ADD FOREIGN KEY (empty_table_3_id) REFERENCES empty_table_3 (id);
ALTER TABLE "Audit".events
  ADD FOREIGN KEY (empty_table_4_id) REFERENCES empty_table_4 (id);
ALTER TABLE "Audit".events
  ADD FOREIGN KEY (empty_table_5_id) REFERENCES empty_table_5 (id);


CREATE INDEX ON homework_grades (student_id);
CREATE INDEX ON homework_grades (assignment_type);
CREATE INDEX ON homework_grades (assignment_id);
ALTER TABLE homework_grades
  ADD FOREIGN KEY (student_id) REFERENCES "Students" (student_id);

