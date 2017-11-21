INSERT INTO self_referencing_table (id, parent_id)
VALUES
  (1, 1), -- edge case: it references not only its own table, but its own row in that table
  (2, 1),
  (3, 2),
  (4, 7),
  (5, 1),
  (6, 3),
  (7, 4),
  (8, 1),
  (9, 1),
  (10, 2),
  (11, 3),
  (12, 4),
  (13, 5),
  (14, 6),
  (15, 7),
  (16, 15);