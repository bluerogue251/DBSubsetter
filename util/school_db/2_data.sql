INSERT INTO districts (name, created_at, updated_at)
  SELECT
    'District # ' || seq,
    now(),
    now()
  FROM generate_series(0, 100) AS seq;

INSERT INTO schools (district_id, name, mascot, created_at, updated_at)
  SELECT
    trunc(random() * 100 + 1),
    'School # ' || seq,
    'Mascot # ' || seq,
    now(),
    now()
  FROM generate_series(0, 10000) AS seq;

CREATE EXTENSION "uuid-ossp";
INSERT INTO "Students" (student_id, name, date_of_birth, current_school_id_cache, created_at, updated_at)
  SELECT
    uuid_generate_v4(),
    'Student # ' || seq,
    '1950-01-01' :: DATE + (seq % 10000 || ' days') :: INTERVAL,
    trunc(random() * 10000 + 1),
    now(),
    now()
  FROM generate_series(0, 1000000) AS seq;
UPDATE "Students"
SET current_school_id_cache = NULL
WHERE date_of_birth < '1970-01-01';

-- Insert assignments for schools that students had previously been assigned to, but no longer are
INSERT INTO school_assignments (school_id, student_id, assignment_start, assignment_end, created_at, updated_at)
  SELECT
    trunc(random() * 10000 + 1),
    student_id,
    '1900-01-01',
    '1904-01-01',
    now(),
    now()
  FROM "Students"
  WHERE random() < 0.01;

-- Insert assignments for schools that students are currently assigned to
-- `on conflict` clause to account for unique constraint
INSERT INTO school_assignments (school_id, student_id, assignment_start, assignment_end, created_at, updated_at)
  SELECT
    current_school_id_cache,
    student_id,
    '1904-01-01',
    NULL,
    now(),
    now()
  FROM "Students"
  WHERE current_school_id_cache IS NOT NULL
ON CONFLICT DO NOTHING;

-- Populate latest valedictorian cache (leave 1/10th of these null)
UPDATE schools
SET latest_valedictorian_id_cache = (SELECT student_id
                                     FROM school_assignments
                                     WHERE school_id = schools.id
                                     ORDER BY random()
                                     LIMIT 1)
WHERE random() < 0.9;

INSERT INTO "Audit".event_types (key) VALUES
  ('enrollment'),
  ('standardized_testing'),
  ('graduation'),
  ('student_class_attendance');

-- Insert some enrollment events
INSERT INTO "Audit".events (id, event_type_key, district_id, school_id, student_id, school_assignment_school_id, school_assignment_student_id, created_at)
  SELECT
    uuid_generate_v4(),
    'enrollment',
    sc.district_id,
    sc.id,
    sa.student_id,
    sa.school_id,
    sa.student_id,
    now()
  FROM school_assignments sa
    INNER JOIN schools sc ON sa.school_id = sc.id;

-- Insert some enrollment events
INSERT INTO "Audit".events (id, event_type_key, district_id, school_id, created_at)
  SELECT
    uuid_generate_v4(),
    'standardized_testing',
    sc.district_id,
    sc.id,
    now()
  FROM schools sc;

-- Insert some graduation events
INSERT INTO "Audit".events (id, event_type_key, district_id, school_id, student_id, created_at)
  SELECT
    uuid_generate_v4(),
    'graduation',
    sc.district_id,
    sc.id,
    sa.student_id,
    sa.assignment_end
  FROM school_assignments sa
    INNER JOIN schools sc ON sa.school_id = sc.id
  WHERE sa.assignment_end IS NOT NULL;

-- Insert a large number of events representing "the fact that a student attended a particular class on a particular day"
-- This is meant to be an example of a 1 to n relationship with a very high cardinality
INSERT INTO "Audit".events (id, event_type_key, district_id, school_id, student_id, school_assignment_school_id, school_assignment_student_id, created_at)
  SELECT
    uuid_generate_v4(),
    'student_class_attendance',
    sc.district_id,
    sc.id,
    sa.student_id,
    sa.school_id,
    sa.student_id,
    now()
  FROM school_assignments sa
    INNER JOIN schools sc ON sa.school_id = sc.id
    CROSS JOIN generate_series(0, 100) AS seq; -- (0, 2000) would generate ~ 100 GB of data with indices

-- "Polymorphic foreign key" data
INSERT INTO multiple_choice_assignments (id, assignment_name, created_at) VALUES
  (1, 'Biology 1 Midterm - Take Home', now()),
  (2, 'Biology 1 Final - Take Home', now()),
  (3, 'Biology 2 Midterm - Take Home', now()),
  (4, 'Biology 2 Final Exam - Take Home', now());

INSERT INTO worksheet_assignments (id, worksheet_assignment_name) VALUES
  (1, 'Chemistry 1 Midterm - Take Home'),
  (2, 'Chemistry 1 Final - Take Home'),
  (3, 'Chemistry 2 Midterm - Take Home'),
  (4, 'Chemistry 2 Final - Take Home');

INSERT INTO essay_assignments (id, name, created_at) VALUES
  (1, 'Persuasive Writing Assignment', now()),
  (2, 'Personal Narrative Assignment', now()),
  (3, 'Journalism Midterm', now()),
  (4, 'Journalism Final', now()),
  (5, 'Biographical Writing Assignment', now());

INSERT INTO homework_grades (student_id, assignment_type, assignment_id, grade, autograded, created_at, updated_at)
  SELECT
    s.student_id,
    (SELECT sub.*
     FROM (SELECT unnest(ARRAY ['worksheet', 'essay', 'multiple choice'])) sub
     ORDER BY random()
     LIMIT 1),
    (SELECT sub.*
     FROM (SELECT unnest(ARRAY [1, 2, 3, 4])) sub
     ORDER BY random()
     LIMIT 1),
    random(),
    (SELECT sub.*
     FROM (SELECT unnest(ARRAY [TRUE, FALSE])) sub
     ORDER BY random()
     LIMIT 1),
    now(),
    now()
  FROM "Students" s
    CROSS JOIN generate_series(0, 100) AS seq;

-- Isolated table data
INSERT INTO standalone_table (note, created_on) VALUES
  ('Note # 1', '1990-01-01'),
  ('Note # 2', '1991-01-01'),
  ('Note # 3', '1992-01-01'),
  ('Note # 4', '1993-01-01');