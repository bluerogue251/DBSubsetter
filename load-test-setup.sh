#!/usr/bin/env bash

set -eou pipefail

sudo snap install docker # in the future try to lock this down to docker version 18.06.1-ce, build e68fc7a

mkdir --parents /home/ubuntu/pgdata/schooldb/origin

sudo docker run \
  --detach \
  --name pg_school_db_origin \
  --volume /home/ubuntu/pgdata/schooldb/origin:/var/lib/postgresql/data \
  -p 5432:5432 \
  postgres:9.6.3 \
  postgres -c 'max_connections=15'

sudo docker run \
  --detach \
  --name pg_school_db_target \
  -p 5433:5432 \
  postgres:9.6.3 \
  postgres -c 'max_connections=15'

sudo docker exec pg_school_db_origin createdb --user postgres school_db
sudo docker exec pg_school_db_target createdb --user postgres school_db

DUMP_URL="https://s3.amazonaws.com/db-subsetter/load-test/school-db/pgdump.sql.gz"
wget -q -O - "${DUMP_URL}" | gunzip | sudo docker exec --interactive --env pg_school_db_origin psql --user postgres --dbname school_db
sudo docker exec pg_school_db_origin psql --user postgres --dbname school_db -c "VACUUM ANALYZE"

sudo docker exec pg_school_db_origin pg_dump --user postgres --dbname school_db --section pre-data | \
  sudo docker exec --interactive pg_school_db_target psql --user postgres --dbname school_db