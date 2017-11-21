#!/usr/bin/env bash

set -e

docker rm --force --volumes self_referencing_target

docker create --name self_referencing_target -p 5521:5432 postgres:9.6.3

docker start self_referencing_target

sleep 5

createdb -p 5521 -h localhost -U postgres self_referencing_target

pg_dump -h localhost -p 5520 -U postgres --section=pre-data self_referencing_origin | psql -h localhost -p 5521 -U postgres self_referencing_target -v ON_ERROR_STOP=1