CREATE TABLE arrays_table (
  id            SERIAL PRIMARY KEY,
  string_array  VARCHAR [],
  int_array     INT [],
  decimal_array DECIMAL [],
  nested_array  VARCHAR [] []
);
-- Still need array of an enum and array of json, array of jsonb, etc.


CREATE TABLE money_table (
  id    SERIAL PRIMARY KEY,
  money MONEY
);

CREATE TABLE binary_table (
  id    SERIAL PRIMARY KEY,
  bytea BYTEA
);

CREATE TABLE times_table (
  id                          SERIAL PRIMARY KEY,
  timestamp_without_time_zone TIMESTAMP WITHOUT TIME ZONE,
  timestamp_with_time_zone    TIMESTAMP WITH TIME ZONE,
  date                        DATE,
  time_without_time_zone      TIME WITHOUT TIME ZONE,
  time_with_time_zone         TIME WITH TIME ZONE,
  interval                    INTERVAL
);

CREATE TYPE MOOD AS ENUM ('sad', 'ok', 'happy');
CREATE TABLE enum_table (
  id   SERIAL PRIMARY KEY,
  enum MOOD
);

CREATE TABLE geometric_table (
  id      SERIAL PRIMARY KEY,
  point   POINT,
  line    LINE,
  lseg    LSEG,
  box     BOX,
  path    PATH,
  polygon POLYGON,
  circle  CIRCLE
);

CREATE TABLE network_address_table (
  id      SERIAL PRIMARY KEY,
  cidr    CIDR,
  inet    INET,
  macaddr MACADDR
);

CREATE TABLE bit_string_table (
  id                    SERIAL PRIMARY KEY,
  bit_1                 BIT,
  bit_5                 BIT(5),
  bit_varying_unlimited BIT VARYING,
  bit_varying_10        BIT VARYING(10)
);

CREATE TABLE text_search_table (
  id       SERIAL PRIMARY KEY,
  tsvector TSVECTOR,
  tsquery  TSQUERY
);

CREATE TABLE xml_table (
  id  SERIAL PRIMARY KEY,
  xml XML
);

CREATE TABLE json_table (
  id    SERIAL PRIMARY KEY,
  json  JSON,
  jsonb JSONB
);

CREATE EXTENSION hstore;
CREATE TABLE hstore_table (
  id     SERIAL PRIMARY KEY,
  hstore HSTORE
);

CREATE TABLE range_table (
  id        SERIAL PRIMARY KEY,
  int4range INT4RANGE,
  int8range INT8RANGE,
  numrange  NUMRANGE,
  tsrange   TSRANGE,
  tstzrange TSTZRANGE,
  daterange DATERANGE
)