CREATE TABLE grandparents (
  id SERIAL PRIMARY KEY
);

CREATE TABLE parents (
  id             SERIAL PRIMARY KEY,
  grandparent_id INTEGER NOT NULL REFERENCES grandparents (id)
);

CREATE TABLE children (
  id        SERIAL PRIMARY KEY,
  parent_id INTEGER NOT NULL REFERENCES parents (id)
);

ALTER TABLE grandparents
  ADD COLUMN favorite_parent_id INTEGER NULL REFERENCES parents (id);
