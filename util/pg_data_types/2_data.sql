INSERT INTO arrays_table (id, string_array, int_array, decimal_array, nested_array) VALUES
  (1, ARRAY ['str_one', 'str_two', 'str_three', 'str_four'], NULL, NULL, NULL),
  (2, NULL, ARRAY [1, 2, 3, 4, 5], NULL, NULL),
  (3, NULL, NULL, ARRAY [2.87, 5893.232345, 1234.90, 9], NULL),
  (4, NULL, NULL, NULL,
   ARRAY [ARRAY ['one_one', 'one_two'], ARRAY ['two_one', 'two_two'], ARRAY ['three_one', 'three_two']]),
  (5, ARRAY ['one', 'two'], ARRAY [1, 2], ARRAY [1.1, 2.2], ARRAY [ARRAY ['one'], ARRAY ['two']]),
  (6, '{}', '{}', '{}', '{}');

INSERT INTO money_table (id, money) VALUES
  (1, '$1,000.00'),
  (2, NULL);

INSERT INTO binary_table (id, bytea) VALUES
  (1, E'\\000' :: BYTEA),
  (2, E'\'' :: BYTEA),
  (3, E'\\\\' :: BYTEA),
  (4, E'\\001' :: BYTEA),
  (5, NULL);

INSERT INTO times_table
(id, timestamp_without_time_zone, timestamp_with_time_zone, date, time_without_time_zone, time_with_time_zone, interval)
VALUES
  (1, '2017-11-18 13:26:07.888767', '2017-11-18 18:26:51.934744 +01:00', '1999-01-01', '13:28:00 ', '12:27:46 -06:00',
   '12 years');

INSERT INTO enum_table (id, enum)
VALUES
  (1, 'sad'),
  (2, 'happy'),
  (3, 'ok'),
  (4, NULL);

INSERT INTO geometric_table (id, point, line, lseg, box, path, polygon, circle)
VALUES
  (1, '3, 9', '{3, 7, 9}', '[(1, 2), (3, 4)]', '((1, 1), (5, 5))', '[(1,1),(2,2),(3,3),(4,4)]',
   '((1,1),(2,2),(3,3),(4,4),(5,5))', '<(78,91), 3>'),
  (2, NULL, NULL, NULL, NULL, '((1,1),(2,2),(3,3),(4,4))', NULL, NULL),
  (3, NULL, NULL, NULL, NULL, NULL, NULL, NULL);

INSERT INTO network_address_table (id, cidr, inet, macaddr)
VALUES
  (1, '192.168.100.128/25', '2001:4f8:3:ba::/64', '08:00:2b:01:02:03'),
  (2, NULL, NULL, NULL);

INSERT INTO bit_string_table (id, bit_1, bit_5, bit_varying_unlimited, bit_varying_10)
VALUES
  (1, '1', '00100', '100101101101010111111', '01'),
  (2, '0', '00101', '', '1111111111'),
  (3, NULL, NULL, NULL, NULL);

INSERT INTO text_search_table (id, tsvector, tsquery)
VALUES
  (1, 'a cat sat on a mat and ate a rat', NULL),
  (2, NULL, 'cat & rat');

INSERT INTO xml_table (id, xml)
VALUES
  (1, XMLPARSE(DOCUMENT '<?xml version="1.0"?><book><title>Manual</title><chapter>...</chapter></book>')),
  (2, XMLPARSE(CONTENT 'abc<foo>bar</foo><bar>foo</bar>')),
  (3, NULL);

INSERT INTO json_table (id, json, jsonb)
VALUES
  (1, '{"bar": "baz", "balance": 7.77, "active":false}', '{"bar": "baz", "balance": 7.77, "active":false}'),
  (2, NULL, NULL);

INSERT INTO hstore_table (id, hstore)
VALUES
  (1, 'a=>b, b=>1, c=>NULL'),
  (2, NULL);

INSERT INTO range_table (id, int4range, int8range, numrange, tsrange, tstzrange, daterange)
VALUES
  (1, '[1, 4]', '[1, 4]', '[2.3, 7.9]', '[2017-11-18 15:01:58.989987, 2018-11-12 15:01:58.989999]',
   '[2011-11-18 15:01:58.989988 -04:00, 2022-11-11 15:01:58.989991 +01:00]', '[2011-09-25, 2012-01-01]'),
  (2, '(1, 4)', '(1, 4)', '(2.3, 7.9)', '(2017-11-18 15:01:58.989987, 2018-11-12 15:01:58.989999)',
   '(2011-11-18 15:01:58.989988 -04:00, 2022-11-11 15:01:58.989991 +01:00)', '(2011-09-25, 2012-01-01)'),
  (3, '(1, 4]', '(1, 4]', '(2.3, 7.9]', '(2017-11-18 15:01:58.989987, 2018-11-12 15:01:58.989999]',
   '(2011-11-18 15:01:58.989988 -04:00, 2022-11-11 15:01:58.989991 +01:00]', '(2011-09-25, 2012-01-01]'),
  (4, '[1, 4)', '[1, 4)', '[2.3, 7.9)', '[2017-11-18 15:01:58.989987, 2018-11-12 15:01:58.989999)',
   '[2011-11-18 15:01:58.989988 -04:00, 2022-11-11 15:01:58.989991 +01:00)', '[2011-09-25, 2012-01-01)'),
  (5, NULL, NULL, NULL, NULL, NULL, NULL);

INSERT INTO uuid_table (id)
VALUES
  ('9bd7b1f8-7175-42d6-8ac9-3b16c67d3686'),
  ('2d19bf31-7f45-4e31-90ef-c81410f2876f');

INSERT INTO uuid_child_table (id, uuid_table_id)
VALUES
  (1, '2d19bf31-7f45-4e31-90ef-c81410f2876f'),
  (3000000000, '9bd7b1f8-7175-42d6-8ac9-3b16c67d3686'),
  (4000000001, NULL);