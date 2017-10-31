#!/usr/bin/env bash

docker rm --force --volumes db_subsetter_origin
docker rm --force --volumes db_subsetter_target
docker create --name db_subsetter_origin -p 5450:5432 postgres:9.6.3
docker create --name db_subsetter_target -p 5451:5432 postgres:9.6.3
docker start db_subsetter_origin
docker start db_subsetter_target
sleep 5

createdb -p 5450 -h localhost -U postgres db_subsetter_origin
createdb -p 5451 -h localhost -U postgres db_subsetter_target

psql -f util/setup_1_schema.sql -p 5450 -h localhost -U postgres db_subsetter_origin -v ON_ERROR_STOP=1
psql -f util/setup_2_data.sql -p 5450 -h localhost -U postgres db_subsetter_origin -v ON_ERROR_STOP=1
psql -f util/setup_3_indices.sql -p 5450 -h localhost -U postgres db_subsetter_origin -v ON_ERROR_STOP=1

pg_dump -h localhost -p 5450 -U postgres --section=pre-data db_subsetter_origin | psql -h localhost -p 5451 -U postgres db_subsetter_target -v ON_ERROR_STOP=1