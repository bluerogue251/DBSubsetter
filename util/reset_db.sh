#!/usr/bin/env bash

docker rm --force --volumes db_subsetter_origin
docker create --name db_subsetter_origin -p 5450:5432 postgres:9.6.3
docker start db_subsetter_origin
sleep 5
createdb -p 5450 -h localhost -U postgres db_subsetter_origin
psql -f util/setup_test_schema.sql -p 5450 -h localhost -U postgres db_subsetter_origin