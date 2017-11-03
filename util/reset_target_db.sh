#!/usr/bin/env bash

set -e

docker rm --force --volumes db_subsetter_target

docker create --name db_subsetter_target -p 5451:5432 postgres:9.6.3

docker start db_subsetter_target

sleep 5

createdb -p 5451 -h localhost -U postgres db_subsetter_target

pg_dump -h localhost -p 5450 -U postgres --section=pre-data db_subsetter_origin | psql -h localhost -p 5451 -U postgres db_subsetter_target -v ON_ERROR_STOP=1