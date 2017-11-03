#!/usr/bin/env bash

set -e

docker rm --force --volumes mouse_genome_origin
docker rm --force --volumes mouse_genome_target

docker create --name mouse_genome_origin -p 5470:5432 postgres:9.6.3
docker create --name mouse_genome_target -p 5471:5432 postgres:9.6.3

docker start mouse_genome_origin
docker start mouse_genome_target

sleep 5

createdb -p 5470 -h localhost -U postgres mouse_genome_origin
createdb -p 5471 -h localhost -U postgres mouse_genome_target

pg_restore -c -d mouse_genome_origin --jobs 4 --no-owner --host localhost -p 5470 -U postgres --no-acl --format=custom ~/Downloads/mgd.postgres.dump

pg_dump -h localhost -p 5470 -U postgres --section=pre-data mouse_genome_origin | psql -h localhost -p 5471 -U postgres mouse_genome_target -v ON_ERROR_STOP=1