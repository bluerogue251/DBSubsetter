insert into tinyints_signed(id) values (-128), (-127), (-1), (0), (1), (126), (127);
insert into tinyints_unsigned(id) values (0), (1), (2), (253), (254), (255);

insert into smallints_signed(id) values (-32768), (-32767), (-1), (0), (1), (32766), (32767);
insert into smallints_unsigned(id) values (0), (1), (2), (65533), (65534), (65535);

insert into mediumints_signed(id) values (-8388608), (-8388607), (-1), (0), (1), (8388606), (8388607);
insert into mediumints_unsigned(id) values (0), (1), (2), (16777213), (16777214), (16777215);

insert into ints_signed(id) values (-2147483648), (-2147483647), (-1), (0), (1), (2147483646), (2147483647);
insert into ints_unsigned(id) values (0), (1), (2), (4294967293), (4294967294), (4294967295);

insert into bigints_signed(id) values (-9223372036854775808), (-9223372036854775807), (-1), (0), (1), (9223372036854775806), (9223372036854775807);
insert into bigints_unsigned(id) values (0), (1), (2), (18446744073709551613), (18446744073709551614), (18446744073709551615);

insert into referencing_table (
  id,
  tinyints_signed_id,
  tinyints_unsigned_id,
  smallints_signed_id,
  smallints_unsigned_id,
  mediumints_signed_id,
  mediumints_unsigned_id,
  ints_signed_id,
  ints_unsigned_id,
  bigints_signed_id,
  bigints_unsigned_id
) VALUES
  (1,  null, null, null,   null,  null,     null,     null,        null,       null,                 null),
  (2,  -128, null, null,   null,  null,     null,     null,        null,       null,                 null),
  (3,  0,    null, null,   null,  null,     null,     null,        null,       null,                 null),
  (4,  127,  null, null,   null,  null,     null,     null,        null,       null,                 null),
  (5,  null, 0,    null,   null,  null,     null,     null,        null,       null,                 null),
  (6,  null, 254,  null,   null,  null,     null,     null,        null,       null,                 null),
  (7,  null, 255,  null,   null,  null,     null,     null,        null,       null,                 null),
  (8,  null, null, -32768, null,  null,     null,     null,        null,       null,                 null),
  (9,  null, null, 0,      null,  null,     null,     null,        null,       null,                 null),
  (10, null, null, 32767,  null,  null,     null,     null,        null,       null,                 null),
  (11, null, null, null,   0,     null,     null,     null,        null,       null,                 null),
  (12, null, null, null,   65534, null,     null,     null,        null,       null,                 null),
  (13, null, null, null,   65535, null,     null,     null,        null,       null,                 null),
  (14, null, null, null,   null,  -8388608, null,     null,        null,       null,                 null),
  (15, null, null, null,   null,  0,        null,     null,        null,       null,                 null),
  (16, null, null, null,   null,  8388607,  null,     null,        null,       null,                 null),
  (17, null, null, null,   null,  null,     0,        null,        null,       null,                 null),
  (18, null, null, null,   null,  null,     16777214, null,        null,       null,                 null),
  (19, null, null, null,   null,  null,     16777215, null,        null,       null,                 null),
  (20, null, null, null,   null,  null,     null,     -2147483648, null,       null,                 null),
  (21, null, null, null,   null,  null,     null,     0,           null,       null,                 null),
  (22, null, null, null,   null,  null,     null,     2147483647,  null,       null,                 null),
  (23, null, null, null,   null,  null,     null,     null,        0,          null,                 null),
  (24, null, null, null,   null,  null,     null,     null,        4294967294, null,                 null),
  (25, null, null, null,   null,  null,     null,     null,        4294967295, null,                 null),
  (26, null, null, null,   null,  null,     null,     null,        null,       -9223372036854775808, null),
  (27, null, null, null,   null,  null,     null,     null,        null,       0,                    null),
  (28, null, null, null,   null,  null,     null,     null,        null,       9223372036854775807,  null),
  (29, null, null, null,   null,  null,     null,     null,        null,       null,                 0),
  (30, null, null, null,   null,  null,     null,     null,        null,       null,                 18446744073709551614),
  (31, null, null, null,   null,  null,     null,     null,        null,       null,                 18446744073709551615);