#!/usr/bin/env bash

set -e

docker rm --force --volumes circular_dep_children_origin

docker create --name circular_dep_children_origin -p 5480:5432 postgres:9.6.3

docker start circular_dep_children_origin

sleep 5

createdb -p 5480 -h localhost -U postgres circular_dep_children_origin

psql -f util/school_db/1_pre_data.sql -p 5480 -h localhost -U postgres circular_dep_children_origin -v ON_ERROR_STOP=1
psql -f util/school_db/2_data.sql -p 5480 -h localhost -U postgres circular_dep_children_origin -v ON_ERROR_STOP=1
psql -f util/school_db/3_post_data.sql -p 5480 -h localhost -U postgres circular_dep_children_origin -v ON_ERROR_STOP=1