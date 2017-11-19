#!/usr/bin/env bash

set -e

docker rm --force --volumes pg_data_types_origin

docker create --name pg_data_types_origin -p 5500:5432 postgres:9.6.3

docker start pg_data_types_origin

sleep 5

createdb -p 5500 -h localhost -U postgres pg_data_types_origin

psql -f util/pg_data_types/1_pre_data.sql -p 5500 -h localhost -U postgres pg_data_types_origin -v ON_ERROR_STOP=1
psql -f util/pg_data_types/2_data.sql -p 5500 -h localhost -U postgres pg_data_types_origin -v ON_ERROR_STOP=1
psql -f util/pg_data_types/3_post_data.sql -p 5500 -h localhost -U postgres pg_data_types_origin -v ON_ERROR_STOP=1