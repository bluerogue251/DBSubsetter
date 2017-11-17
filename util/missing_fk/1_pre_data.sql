CREATE TABLE table_1 (
  id SERIAL PRIMARY KEY
);

CREATE TABLE table_2 (
  id         SERIAL PRIMARY KEY,
  table_1_id INTEGER NOT NULL REFERENCES table_1 (id)
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