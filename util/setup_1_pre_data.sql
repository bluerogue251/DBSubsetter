CREATE TABLE districts (
  id         SERIAL PRIMARY KEY,
  name       TEXT      NOT NULL,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL
);

CREATE TABLE schools (
  id          SERIAL PRIMARY KEY,
  district_id INTEGER   NOT NULL REFERENCES districts (id),
  name        TEXT      NOT NULL,
  mascot      TEXT      NOT NULL,
  created_at  TIMESTAMP NOT NULL,
  updated_at  TIMESTAMP NOT NULL
);

CREATE TABLE students (
  student_id              UUID PRIMARY KEY,
  name                    TEXT      NOT NULL,
  date_of_birth           DATE      NULL,
  current_school_id_cache INTEGER   NULL REFERENCES schools (id),
  created_at              TIMESTAMP NOT NULL,
  updated_at              TIMESTAMP NOT NULL
);

CREATE TABLE school_assignments (
  school_id        INTEGER   NOT NULL REFERENCES schools (id),
  student_id       UUID      NOT NULL REFERENCES students (student_id),
  assignment_start DATE      NOT NULL,
  assignment_end   DATE      NULL,
  created_at       TIMESTAMP NOT NULL,
  updated_at       TIMESTAMP NOT NULL,
  PRIMARY KEY (school_id, student_id) -- Composite primary key
);

-- Circular dependency
ALTER TABLE schools
  ADD COLUMN latest_valedictorian_id_cache UUID NULL REFERENCES students (student_id);

CREATE TABLE homework_grades (
  id            BIGSERIAL PRIMARY KEY, -- BIGSERIAL pk type
  student_id    UUID      NOT NULL REFERENCES students (student_id),
  homework_type TEXT      NOT NULL,
  grade         DECIMAL(5, 2), -- Number between 0.00 and 100.00 representing the grade. Max of two decimal places.
  created_at    TIMESTAMP NOT NULL,
  autograded    BOOLEAN   NULL, -- Boolean
  updated_at    TIMESTAMP NOT NULL
);

CREATE SCHEMA audit;

CREATE TABLE audit.event_types (
  key VARCHAR(30) PRIMARY KEY -- Character-based primary key
);

CREATE TABLE audit.events (
  id                           UUID PRIMARY KEY,
  event_type_key               VARCHAR(30) NOT NULL REFERENCES audit.event_types (key),
  district_id                  INTEGER     NULL REFERENCES districts (id),
  school_id                    INTEGER     NULL REFERENCES schools (id),
  student_id                   UUID        NULL REFERENCES students (student_id),
  school_assignment_school_id  INTEGER     NULL,
  school_assignment_student_id UUID        NULL,
  created_at                   TIMESTAMP   NOT NULL,
  FOREIGN KEY (school_assignment_school_id, school_assignment_student_id) REFERENCES school_assignments (school_id, student_id) -- Composite foreign key
);