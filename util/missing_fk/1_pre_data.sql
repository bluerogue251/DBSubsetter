CREATE TABLE table_1 (
  id SERIAL PRIMARY KEY
);

CREATE TABLE table_2 (
  id         SERIAL PRIMARY KEY,
  table_1_id INTEGER NOT NULL -- This points to table_1 (id), but we purposely leave off the FK at the DB level
);

CREATE TABLE table_3 (
  id SERIAL PRIMARY KEY
);

-- This table's "primary key" is defined by a tuple (table_1_id, table_3_id)
-- But this primary key is not defined at the DB level. We have to define it as user-supplied config
CREATE TABLE table_4 (
  table_1_id INTEGER NOT NULL,
  table_3_id INTEGER NOT NULL,
  UNIQUE (table_1_id, table_3_id),
  FOREIGN KEY (table_1_id) REFERENCES table_1 (id),
  FOREIGN KEY (table_3_id) REFERENCES table_3 (id)
);



CREATE TABLE table_5 (
  id                 SERIAL PRIMARY KEY,
  table_4_table_1_id INTEGER NOT NULL,
  table_4_table_3_id INTEGER NOT NULL,
  FOREIGN KEY (table_4_table_1_id, table_4_table_3_id) REFERENCES table_4 (table_1_id, table_3_id)
);

CREATE TABLE table_a (
  id                   SERIAL PRIMARY KEY,
  points_to_table_name VARCHAR(255) NOT NULL,
  points_to_table_id   INTEGER      NOT NULL
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