#!/usr/bin/env bash

set -e

docker rm --force --volumes school_db_origin

docker create --name school_db_origin -p 5450:5432 postgres:9.6.3

docker start school_db_origin

sleep 5

createdb -p 5450 -h localhost -U postgres school_db_origin

psql -f util/school_db/1_pre_data.sql -p 5450 -h localhost -U postgres school_db_origin -v ON_ERROR_STOP=1
psql -f util/school_db/2_data.sql -p 5450 -h localhost -U postgres school_db_origin -v ON_ERROR_STOP=1
psql -f util/school_db/3_post_data.sql -p 5450 -h localhost -U postgres school_db_origin -v ON_ERROR_STOP=1