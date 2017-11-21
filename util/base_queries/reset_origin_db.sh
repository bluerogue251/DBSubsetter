#!/usr/bin/env bash

set -e

docker rm --force --volumes base_queries_origin

docker create --name base_queries_origin -p 5510:5432 postgres:9.6.3

docker start base_queries_origin

sleep 5

createdb -p 5510 -h localhost -U postgres base_queries_origin

psql -f util/base_queries/1_pre_data.sql -p 5510 -h localhost -U postgres base_queries_origin -v ON_ERROR_STOP=1
psql -f util/base_queries/2_data.sql -p 5510 -h localhost -U postgres base_queries_origin -v ON_ERROR_STOP=1
psql -f util/base_queries/3_post_data.sql -p 5510 -h localhost -U postgres base_queries_origin -v ON_ERROR_STOP=1