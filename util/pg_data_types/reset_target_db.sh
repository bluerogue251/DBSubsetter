#!/usr/bin/env bash

set -e

docker rm --force --volumes pg_data_types_target

docker create --name pg_data_types_target -p 5501:5432 postgres:9.6.3

docker start pg_data_types_target

sleep 5

createdb -p 5501 -h localhost -U postgres pg_data_types_target

pg_dump -h localhost -p 5500 -U postgres --section=pre-data pg_data_types_origin | psql -h localhost -p 5501 -U postgres pg_data_types_target -v ON_ERROR_STOP=1