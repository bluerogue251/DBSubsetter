package e2e.circulardep

import slick.jdbc.JdbcProfile

class CircularDepDML(val profile: JdbcProfile) extends CircularDepDDL {

  def dbioSeq = {
    slick.dbio.DBIO.seq()

    //    INSERT INTO grandparents (id)
    //    SELECT seq
    //      FROM generate_series(0, 1000) AS seq;
    //
    //    INSERT INTO parents (id, grandparent_id)
    //    SELECT
    //    seq,
    //    floor(seq / 10)
    //    FROM generate_series(0, 10009) AS seq; -- 10 parents for every grandparent
    //
    //    INSERT INTO children (id, parent_id)
    //    SELECT
    //    seq,
    //    floor(seq / 5)
    //    FROM generate_series(0, 50049) AS seq; -- 5 children for every parent
    //
    //    UPDATE grandparents
    //      SET favorite_parent_id = seq * 10
    //    FROM generate_series(0, 1000) AS seq
    //    WHERE
    //    seq = grandparents.id AND grandparents.id % 3 = 0; -- Only populate a third of them to make the dataset more varied
  }
}


