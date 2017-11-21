CREATE TABLE base_table (
  id SERIAL PRIMARY KEY
);

CREATE TABLE child_table (
  id            SERIAL PRIMARY KEY,
  base_table_id INTEGER REFERENCES base_table (id)
);
