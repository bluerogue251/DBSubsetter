#!/usr/bin/env bash

set -e

pg_dump -h localhost -p 5450 -U postgres --section=post-data db_subsetter_origin | psql -h localhost -p 5451 -U postgres db_subsetter_target -v ON_ERROR_STOP=1

psql -h localhost -p 5451 -U postgres -d db_subsetter_target -c 'alter table schools add foreign key (district_id) references districts ("Id")'