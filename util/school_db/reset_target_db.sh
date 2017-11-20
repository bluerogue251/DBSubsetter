#!/usr/bin/env bash

set -e

docker rm --force --volumes school_db_target

docker create --name school_db_target -p 5451:5432 postgres:9.6.3

docker start school_db_target

sleep 5

createdb -p 5451 -h localhost -U postgres school_db_target

pg_dump -h localhost -p 5450 -U postgres --section=pre-data school_db_origin | psql -h localhost -p 5451 -U postgres school_db_target -v ON_ERROR_STOP=1