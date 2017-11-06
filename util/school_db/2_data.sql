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
INSERT INTO students (student_id, name, date_of_birth, current_school_id_cache, created_at, updated_at)
  SELECT
    uuid_generate_v4(),
    'Student # ' || seq,
    '1950-01-01' :: DATE + (seq % 10000 || ' days') :: INTERVAL,
    trunc(random() * 10000 + 1),
    now(),
    now()
  FROM generate_series(0, 1000000) AS seq;
UPDATE students
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
  FROM students
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
  FROM students
  WHERE current_school_id_cache IS NOT NULL
ON CONFLICT DO NOTHING;

-- Populate latest valedictorian cache (watch out this is a circular dependency) (leave 1/10th of these null)
UPDATE schools
SET latest_valedictorian_id_cache = (SELECT student_id
                                     FROM school_assignments
                                     WHERE school_id = schools.id
                                     ORDER BY random()
                                     LIMIT 1)
WHERE random() < 0.9;

INSERT INTO audit.event_types (key) VALUES
  ('enrollment'),
  ('standardized_testing'),
  ('graduation'),
  ('student_class_attendance');

-- Insert some enrollment events
INSERT INTO audit.events (id, event_type_key, district_id, school_id, student_id, school_assignment_school_id, school_assignment_student_id, created_at)
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
INSERT INTO audit.events (id, event_type_key, district_id, school_id, created_at)
  SELECT
    uuid_generate_v4(),
    'standardized_testing',
    sc.district_id,
    sc.id,
    now()
  FROM schools sc;

-- Insert some graduation events
INSERT INTO audit.events (id, event_type_key, district_id, school_id, student_id, created_at)
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
INSERT INTO audit.events (id, event_type_key, district_id, school_id, student_id, school_assignment_school_id, school_assignment_student_id, created_at)
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
    CROSS JOIN generate_series(0, 100) AS seq; -- (0, 2000) would generate ~ 66 GB of data


-- TODO: add data for homework_grades