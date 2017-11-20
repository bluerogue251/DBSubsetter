CREATE TABLE districts (
  "Id"       SERIAL PRIMARY KEY, -- Mixed-case column name
  name       TEXT      NOT NULL,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL
);

CREATE TABLE schools (
  district_id INTEGER   NOT NULL REFERENCES districts ("Id"),
  name        TEXT      NOT NULL,
  mascot      TEXT      NULL,
  id          SERIAL PRIMARY KEY, -- Edge case: PK is not first column in table and appears after a column that is marked as excluded (mascot).
  created_at  TIMESTAMP NOT NULL,
  updated_at  TIMESTAMP NOT NULL
);

CREATE TABLE "Students" (-- Mixed case table name
  student_id              BIGSERIAL PRIMARY KEY,
  name                    TEXT      NOT NULL,
  date_of_birth           DATE      NULL,
  current_school_id_cache INTEGER   NULL REFERENCES schools (id),
  created_at              TIMESTAMP NOT NULL,
  updated_at              TIMESTAMP NOT NULL
);

CREATE TABLE school_assignments (
  school_id        INTEGER   NOT NULL REFERENCES schools (id),
  student_id       BIGINT    NOT NULL REFERENCES "Students" (student_id),
  assignment_start DATE      NOT NULL,
  assignment_end   DATE      NULL,
  created_at       TIMESTAMP NOT NULL,
  updated_at       TIMESTAMP NOT NULL,
  PRIMARY KEY (school_id, student_id) -- Composite primary key
);

-- Adding circular dependency on purpose
-- Some legacy DBs have these so we should test against one
ALTER TABLE schools
  ADD COLUMN latest_valedictorian_id_cache BIGINT NULL REFERENCES "Students" (student_id);

CREATE TABLE homework_grades (
  id              BIGSERIAL, -- BIGSERIAL pk type (pk added in post-data)
  student_id      BIGINT    NOT NULL REFERENCES "Students" (student_id),
  assignment_type TEXT      NOT NULL, -- "Polymorphic Foreign Key" (enforced at application-level, not at DB-level)
  assignment_id   INTEGER   NOT NULL, -- with the "type" column deciding which table the "id" column points to
  grade           DECIMAL(5, 2),
  autograded      BOOLEAN   NULL,
  created_at      TIMESTAMP NOT NULL,
  updated_at      TIMESTAMP NOT NULL
);

-- Part of the "Polymorphic Foreign Keys" test
CREATE TABLE essay_assignments (
  id     SERIAL PRIMARY KEY,
  "name" TEXT NOT NULL
);

-- Part of the "Polymorphic Foreign Keys" test
CREATE TABLE multiple_choice_assignments (
  id              SERIAL PRIMARY KEY,
  assignment_name TEXT      NOT NULL,
  created_at      TIMESTAMP NOT NULL
);

-- Part of the "Polymorphic Foreign Keys" test
CREATE TABLE worksheet_assignments (
  id                          SERIAL PRIMARY KEY,
  "worksheet_assignment_name" TEXT NOT NULL
);

CREATE SCHEMA "Audit"; -- Mixed case schema name

CREATE TABLE "Audit".event_types (
  key VARCHAR(30) PRIMARY KEY -- Character-based primary key
);

CREATE TABLE "Audit".events (
  id                           BIGSERIAL,
  event_type_key               VARCHAR(30) NOT NULL,
  district_id                  INTEGER     NULL,
  school_id                    INTEGER     NULL,
  student_id                   BIGINT      NULL,
  school_assignment_school_id  INTEGER     NULL,
  school_assignment_student_id BIGINT      NULL,
  empty_table_1_id             INTEGER     NULL,
  empty_table_2_id             INTEGER     NULL,
  empty_table_3_id             INTEGER     NULL,
  empty_table_4_id             INTEGER     NULL,
  empty_table_5_id             INTEGER     NULL,
  created_at                   TIMESTAMP   NOT NULL
);

-- Tables purposely left empty as edge cases
CREATE TABLE empty_table_1 (
  id SERIAL PRIMARY KEY
);

CREATE TABLE empty_table_2 (
  id SERIAL PRIMARY KEY
);

CREATE TABLE empty_table_3 (
  id SERIAL PRIMARY KEY
);

CREATE TABLE empty_table_4 (
  id SERIAL PRIMARY KEY
);

CREATE TABLE empty_table_5 (
  id SERIAL PRIMARY KEY
);

-- Standalone table purposely not referenced from any other table as edge case
CREATE TABLE standalone_table (
  id         BIGSERIAL PRIMARY KEY,
  note       TEXT NULL,
  created_on DATE
)