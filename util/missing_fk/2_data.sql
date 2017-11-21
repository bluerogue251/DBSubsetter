INSERT INTO table_1 (id) VALUES (1), (2);
INSERT INTO table_2 (id, table_1_id) VALUES (1, 2), (2, 2), (3, 1);
INSERT INTO table_3 (id) VALUES (45), (46), (47), (48), (49), (50);
INSERT INTO table_4 (table_1_id, table_3_id) VALUES (2, 45), (1, 47), (2, 50);
INSERT INTO table_5 (id, table_4_table_1_id, table_4_table_3_id) VALUES (98, 1, 47), (99, 2, 45);

INSERT INTO table_a (id, points_to_table_name, points_to_table_id) VALUES
  (1, 'points_to_table_b', 1),
  (2, 'points_to_table_b', 1),
  (3, 'points_to_table_b', 2),
  (4, 'points_to_table_d', 2),
  -- edge case -- id does not exist in target table
  (5, 'points_to_table_d', 30),
  -- edge case -- Row 6 is NOT part of the subset, so row 1 of table_d should NOT be included
  -- however, row #1 of table_b SHOULD be included.
  -- This helps to test that table_b subsetting is not accidentally leaking over into table_d
  -- This is based on a real bug that used to exist in our code
  (6, 'points_to_table_d', 1);

INSERT INTO table_b (id) VALUES (1), (2), (3);
INSERT INTO table_c (id) VALUES (1), (2), (3);
INSERT INTO table_d (id) VALUES (1), (2), (3);