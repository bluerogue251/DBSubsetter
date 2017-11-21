#!/usr/bin/env bash

set -e

docker rm --force --volumes self_referencing_origin

docker create --name self_referencing_origin -p 5520:5432 postgres:9.6.3

docker start self_referencing_origin

sleep 5

createdb -p 5520 -h localhost -U postgres self_referencing_origin

psql -f util/self_referencing/1_pre_data.sql -p 5520 -h localhost -U postgres self_referencing_origin -v ON_ERROR_STOP=1
psql -f util/self_referencing/2_data.sql -p 5520 -h localhost -U postgres self_referencing_origin -v ON_ERROR_STOP=1
psql -f util/self_referencing/3_post_data.sql -p 5520 -h localhost -U postgres self_referencing_origin -v ON_ERROR_STOP=1