#!/usr/bin/env bash

set -e

docker rm --force --volumes base_queries_target

docker create --name base_queries_target -p 5511:5432 postgres:9.6.3

docker start base_queries_target

sleep 5

createdb -p 5511 -h localhost -U postgres base_queries_target

pg_dump -h localhost -p 5510 -U postgres --section=pre-data base_queries_origin | psql -h localhost -p 5511 -U postgres base_queries_target -v ON_ERROR_STOP=1