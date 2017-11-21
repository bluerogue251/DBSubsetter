INSERT INTO base_table (id) VALUES (1), (2), (3), (4), (5), (6), (7), (8), (9), (10);

INSERT INTO child_table (id, base_table_id) VALUES
  (1, 1),
  (2, 1),
  (3, 2),
  (4, 2),
  (5, 3),
  (6, 3),
  (7, 3),
  (8, 3),
  (9, 4),
  (10, 4),
  (11, 5),
  (12, 5),
  (13, 6),
  (14, 6),
  (15, 7);
