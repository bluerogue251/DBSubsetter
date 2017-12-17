CREATE TABLE tinyints_signed (
  id TINYINT SIGNED PRIMARY KEY
);

CREATE TABLE tinyints_unsigned (
  id TINYINT UNSIGNED PRIMARY KEY
);

CREATE TABLE smallints_signed (
  id SMALLINT SIGNED PRIMARY KEY
);

CREATE TABLE smallints_unsigned (
  id SMALLINT UNSIGNED PRIMARY KEY
);

CREATE TABLE mediumints_signed (
  id MEDIUMINT SIGNED PRIMARY KEY
);

CREATE TABLE mediumints_unsigned (
  id MEDIUMINT UNSIGNED PRIMARY KEY
);

CREATE TABLE ints_signed (
  id INT SIGNED PRIMARY KEY
);

CREATE TABLE ints_unsigned (
  id INT UNSIGNED PRIMARY KEY
);

CREATE TABLE bigints_signed (
  id BIGINT SIGNED PRIMARY KEY
);

CREATE TABLE bigints_unsigned (
  id BIGINT UNSIGNED PRIMARY KEY
);

CREATE TABLE referencing_table (
  id                     INT UNSIGNED PRIMARY KEY,
  tinyints_signed_id     TINYINT SIGNED REFERENCES tinyints_signed (id),
  tinyints_unsigned_id   TINYINT UNSIGNED REFERENCES tinyints_unsigned (id),
  smallints_signed_id    SMALLINT SIGNED REFERENCES smallints_signed (id),
  smallints_unsigned_id  SMALLINT UNSIGNED REFERENCES smallints_unsigned (id),
  mediumints_signed_id   MEDIUMINT SIGNED REFERENCES mediumints_signed (id),
  mediumints_unsigned_id MEDIUMINT UNSIGNED REFERENCES mediumints_unsigned (id),
  ints_signed_id         INT SIGNED REFERENCES ints_signed (id),
  ints_unsigned_id       INT UNSIGNED REFERENCES ints_unsigned (id),
  bigints_signed_id      BIGINT SIGNED REFERENCES bigints_signed (id),
  bigints_unsigned_id    BIGINT UNSIGNED REFERENCES bigints_unsigned (id)
);