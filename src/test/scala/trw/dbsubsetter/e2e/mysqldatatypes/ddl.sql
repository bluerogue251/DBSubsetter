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
  tinyints_signed_id     TINYINT SIGNED,
  tinyints_unsigned_id   TINYINT UNSIGNED,
  smallints_signed_id    SMALLINT SIGNED,
  smallints_unsigned_id  SMALLINT UNSIGNED,
  mediumints_signed_id   MEDIUMINT SIGNED,
  mediumints_unsigned_id MEDIUMINT UNSIGNED,
  ints_signed_id         INT SIGNED,
  ints_unsigned_id       INT UNSIGNED,
  bigints_signed_id      BIGINT SIGNED,
  bigints_unsigned_id    BIGINT UNSIGNED,
  FOREIGN KEY (tinyints_signed_id) REFERENCES tinyints_signed (id),
  FOREIGN KEY (tinyints_unsigned_id) REFERENCES tinyints_unsigned (id),
  FOREIGN KEY (smallints_signed_id) REFERENCES smallints_signed (id),
  FOREIGN KEY (smallints_unsigned_id) REFERENCES smallints_unsigned (id),
  FOREIGN KEY (mediumints_signed_id) REFERENCES mediumints_signed (id),
  FOREIGN KEY (mediumints_unsigned_id) REFERENCES mediumints_unsigned (id),
  FOREIGN KEY (ints_signed_id) REFERENCES ints_signed (id),
  FOREIGN KEY (ints_unsigned_id) REFERENCES ints_unsigned (id),
  FOREIGN KEY (bigints_signed_id) REFERENCES bigints_signed (id),
  FOREIGN KEY (bigints_unsigned_id) REFERENCES bigints_unsigned (id)
);