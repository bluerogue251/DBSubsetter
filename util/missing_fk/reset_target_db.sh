#!/usr/bin/env bash

set -e

docker rm --force --volumes missing_fk_target

docker create --name missing_fk_target -p 5491:5432 postgres:9.6.3

docker start missing_fk_target

sleep 5

createdb -p 5491 -h localhost -U postgres missing_fk_target

pg_dump -h localhost -p 5490 -U postgres --section=pre-data missing_fk_origin | psql -h localhost -p 5491 -U postgres missing_fk_target -v ON_ERROR_STOP=1