INSERT INTO districts (name, created_at, updated_at)
  SELECT
    'District # ' || seq,
    '2017-11-20 11:19:27.054177',
    '2018-12-15 14:19:25.954172'
  FROM generate_series(0, 100) AS seq;

INSERT INTO schools (district_id, name, mascot, created_at, updated_at)
  SELECT
    (seq % 99) + 1,
    'School # ' || seq + 1,
    'Mascot # ' || seq + 1,
    '2013-10-20 10:19:27.235677',
    '2015-10-29 11:14:22.275870'
  FROM generate_series(0, 10000) AS seq;

INSERT INTO "Students" (name, date_of_birth, current_school_id_cache, created_at, updated_at)
  SELECT
    'Student # ' || seq,
    '1950-01-01' :: DATE + (seq % 10000 || ' days') :: INTERVAL,
    CASE WHEN (seq % 3) = 0
      THEN NULL
    ELSE (seq % 9999) + 1 END,
    '1999-10-22 11:12:22.354179',
    '2001-10-23 08:09:21.435177'
  FROM generate_series(0, 1000000) AS seq;
UPDATE "Students"
SET current_school_id_cache = NULL
WHERE date_of_birth < '1970-01-01';

-- Insert assignments for schools that students had previously been assigned to, but no longer are
INSERT INTO school_assignments (school_id, student_id, assignment_start, assignment_end, created_at, updated_at)
  SELECT
    (student_id % 9999) + 1,
    student_id,
    '1900-01-01',
    '1904-01-01',
    '2018-11-20 11:19:27.054171',
    '2019-12-15 14:19:25.954171'
  FROM "Students"
  WHERE student_id % 10 = 0;

-- Insert assignments for schools that students are currently assigned to
-- `on conflict` clause to account for unique constraint
INSERT INTO school_assignments (school_id, student_id, assignment_start, assignment_end, created_at, updated_at)
  SELECT
    current_school_id_cache,
    student_id,
    '1904-01-01',
    NULL,
    '1890-10-24 11:19:27.054177',
    '1895-10-24 11:19:27.999999'
  FROM "Students"
  WHERE current_school_id_cache IS NOT NULL
ON CONFLICT DO NOTHING;

-- Populate latest valedictorian cache
UPDATE schools
SET latest_valedictorian_id_cache = (SELECT student_id
                                     FROM school_assignments
                                     WHERE school_id = schools.id
                                     ORDER BY school_id ASC
                                     LIMIT 1
                                     OFFSET 8)
WHERE schools.id % 10 != 7; -- leave 1/10th of these null for variety

INSERT INTO "Audit".event_types (key) VALUES
  ('enrollment'),
  ('standardized_testing'),
  ('graduation'),
  ('student_class_attendance');

-- Insert some enrollment events
INSERT INTO "Audit".events (event_type_key, district_id, school_id, student_id, school_assignment_school_id, school_assignment_student_id, created_at)
  SELECT
    'enrollment',
    sc.district_id,
    sc.id,
    sa.student_id,
    sa.school_id,
    sa.student_id,
    '2005-10-24 11:19:27.888888'
  FROM school_assignments sa
    INNER JOIN schools sc ON sa.school_id = sc.id;

-- Insert some enrollment events
INSERT INTO "Audit".events (event_type_key, district_id, school_id, created_at)
  SELECT
    'standardized_testing',
    sc.district_id,
    sc.id,
    '2010-10-25 11:19:27.333666'
  FROM schools sc;

-- Insert some graduation events
INSERT INTO "Audit".events (event_type_key, district_id, school_id, student_id, created_at)
  SELECT
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
INSERT INTO "Audit".events (event_type_key, district_id, school_id, student_id, school_assignment_school_id, school_assignment_student_id, created_at)
  SELECT
    'student_class_attendance',
    sc.district_id,
    sc.id,
    sa.student_id,
    sa.school_id,
    sa.student_id,
    '1999-10-25 11:19:27.888999'
  FROM school_assignments sa
    INNER JOIN schools sc ON sa.school_id = sc.id
    CROSS JOIN generate_series(0, 10) AS seq; -- (0, 2000) would generate ~ 200 GB of data once indices are created

-- "Polymorphic foreign key" data
INSERT INTO multiple_choice_assignments (id, assignment_name, created_at) VALUES
  (1, 'Biology 1 Midterm - Take Home', '1950-01-01 00:00:00.000000'),
  (2, 'Biology 1 Final - Take Home', '1950-01-02 00:00:00.000000'),
  (3, 'Biology 2 Midterm - Take Home', '1950-01-03 00:00:00.000000'),
  (4, 'Biology 2 Final Exam - Take Home', '1950-01-04 00:00:00.000000');

INSERT INTO worksheet_assignments (id, worksheet_assignment_name) VALUES
  (1, 'Chemistry 1 Midterm - Take Home'),
  (2, 'Chemistry 1 Final - Take Home'),
  (3, 'Chemistry 2 Midterm - Take Home'),
  (4, 'Chemistry 2 Final - Take Home');

INSERT INTO essay_assignments (id, name) VALUES
  (1, 'Persuasive Writing Assignment'),
  (2, 'Personal Narrative Assignment'),
  (3, 'Journalism Midterm'),
  (4, 'Journalism Final'),
  (5, 'Biographical Writing Assignment');

INSERT INTO homework_grades (student_id, assignment_type, assignment_id, grade, autograded, created_at, updated_at)
  SELECT
    s.student_id,
    CASE WHEN seq % 3 = 0
      THEN 'worksheet'
    WHEN seq % 5 = 0
      THEN 'essay'
    ELSE 'multiple choice' END,
    -- Pick an assignment type (picks a number from 1 to 4)
    seq % 4 + 1,
    -- Pick a grade from 0.00 to ~ 99.50
    seq % 100 + (seq % 50) :: DECIMAL / (seq % 50 + 50) :: DECIMAL,
    CASE WHEN seq % 3 = 0
      THEN TRUE
    ELSE FALSE END,
    now(),
    now()
  FROM "Students" s
    CROSS JOIN generate_series(0, 3) AS seq;

-- Isolated table data
INSERT INTO standalone_table (note, created_on) VALUES
  ('Note # 1', '1990-01-01'),
  ('Note # 2', '1991-01-01'),
  ('Note # 3', '1992-01-01'),
  ('Note # 4', '1993-01-01');