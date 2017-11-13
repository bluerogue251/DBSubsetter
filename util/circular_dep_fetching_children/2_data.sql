INSERT INTO grandparents (id)
  SELECT seq
  FROM generate_series(0, 1000) AS seq;

INSERT INTO parents (id, grandparent_id)
  SELECT
    seq,
    seq / 1000
  FROM generate_series(0, 100000) AS seq;

INSERT INTO children (id, parent_id)
  SELECT
    seq,
    seq / 1000
  FROM generate_series(0, 1000000) AS seq;
