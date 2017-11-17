CREATE TABLE table_1 (
  id SERIAL PRIMARY KEY
);

CREATE TABLE table_2 (
  id         SERIAL PRIMARY KEY,
  table_1_id INTEGER NOT NULL -- This points to table_1 (id), but we purposely leave off the FK at the DB level
);

CREATE TABLE table_a (
  id                   SERIAL PRIMARY KEY,
  points_to_table_name VARCHAR NOT NULL,
  points_to_table_id   INTEGER NOT NULL
);

CREATE TABLE table_b (
  id SERIAL PRIMARY KEY
);

CREATE TABLE table_c (
  id SERIAL PRIMARY KEY
);

CREATE TABLE table_d (
  id SERIAL PRIMARY KEY
);