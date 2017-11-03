CREATE INDEX ON schools (district_id);
CREATE INDEX ON schools (latest_valedictorian_id_cache);
CREATE INDEX ON students (current_school_id_cache);
CREATE INDEX ON school_assignments (school_id);
CREATE INDEX ON school_assignments (student_id);

ALTER TABLE audit.events
  ADD PRIMARY KEY (id);
ALTER TABLE audit.events
  ADD FOREIGN KEY (event_type_key) REFERENCES audit.event_types (key);
ALTER TABLE audit.events
  ADD FOREIGN KEY (district_id) REFERENCES districts (id);
ALTER TABLE audit.events
  ADD FOREIGN KEY (school_id) REFERENCES schools (id);
ALTER TABLE audit.events
  ADD FOREIGN KEY (school_id) REFERENCES schools (id);
ALTER TABLE audit.events
  ADD FOREIGN KEY (student_id) REFERENCES students (student_id);
-- Composite foreign key
ALTER TABLE audit.events
  ADD FOREIGN KEY (school_assignment_school_id, school_assignment_student_id) REFERENCES school_assignments (school_id, student_id);

CREATE INDEX ON audit.events (event_type_key);
CREATE INDEX ON audit.events (district_id);
CREATE INDEX ON audit.events (school_id);
CREATE INDEX ON audit.events (student_id);
CREATE INDEX ON audit.events (school_assignment_school_id);
CREATE INDEX ON audit.events (school_assignment_student_id);