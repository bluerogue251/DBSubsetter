INSERT INTO grandparents (id)
  SELECT seq
  FROM generate_series(0, 1000) AS seq;

INSERT INTO parents (id, grandparent_id)
  SELECT
    seq,
    seq / 100
  FROM generate_series(0, 100000) AS seq; -- 100 parents for every grandparent

INSERT INTO children (id, parent_id)
  SELECT
    seq,
    seq / 10
  FROM generate_series(0, 1000000) AS seq; -- 10 children for every parent

UPDATE grandparents
SET favorite_parent_id = seq * 100
FROM generate_series(0, 1000) AS seq
WHERE
  seq = grandparents.id AND grandparents.id % 3 = 0; -- Only populate a third of them to make the dataset more varied