#!/usr/bin/env bash

set -e

docker rm --force --volumes missing_fk_origin

docker create --name missing_fk_origin -p 5490:5432 postgres:9.6.3

docker start missing_fk_origin

sleep 5

createdb -p 5490 -h localhost -U postgres missing_fk_origin

psql -f util/circular_dep/1_pre_data.sql -p 5490 -h localhost -U postgres missing_fk_origin -v ON_ERROR_STOP=1
psql -f util/circular_dep/2_data.sql -p 5490 -h localhost -U postgres missing_fk_origin -v ON_ERROR_STOP=1
psql -f util/circular_dep/3_post_data.sql -p 5490 -h localhost -U postgres missing_fk_origin -v ON_ERROR_STOP=1