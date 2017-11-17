#!/usr/bin/env bash

set -e

docker rm --force --volumes circular_dep_target

docker create --name circular_dep_target -p 5481:5432 postgres:9.6.3

docker start circular_dep_target

sleep 5

createdb -p 5481 -h localhost -U postgres circular_dep_target

pg_dump -h localhost -p 5480 -U postgres --section=pre-data circular_dep_origin | psql -h localhost -p 5481 -U postgres circular_dep_target -v ON_ERROR_STOP=1