INSERT INTO table_1 (id) VALUES (1), (2);
INSERT INTO table_2 (id, table_1_id) VALUES (1, 2), (2, 2), (3, 1);


CREATE TABLE table_a (
  id                   SERIAL PRIMARY KEY,
  points_to_table_name VARCHAR NOT NULL,
  points_to_table_id   INTEGER NOT NULL
);

INSERT INTO table_a (id, points_to_table_name, points_to_table_id) VALUES
  (1, 'points_to_table_b', 1),
  (2, 'points_to_table_b', 1),
  (3, 'points_to_table_b', 2),
  (4, 'points_to_table_d', 2);

INSERT INTO table_b (id) VALUES (1), (2), (3);
INSERT INTO table_c (id) VALUES (1), (2), (3);
INSERT INTO table_d (id) VALUES (1), (2), (3);