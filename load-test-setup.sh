#!/usr/bin/env bash

set -eou pipefail

# Install docker (in the future try to lock this down to version 18.06.1-ce)
sudo snap install docker

# Install JRE
sudo add-apt-repository -y ppa:openjdk-r/ppa
sudo apt install -y openjdk-8-jre-headless

mkdir --parents /home/ubuntu/pgdata/schooldb/origin
mkdir --parents /home/ubuntu/tmp-data

sudo docker run \
  --detach \
  --name pg_origin \
  --volume /home/ubuntu/pgdata/schooldb/origin:/var/lib/postgresql/data \
  --volume /home/ubuntu/tmp-data:/tmp-data \
  -p 5432:5432 \
  postgres:9.6.3 \
  postgres -c 'max_connections=15'

sudo docker run \
  --detach \
  --name pg_target \
  -p 5433:5432 \
  postgres:9.6.3 \
  postgres -c 'max_connections=15'

sudo docker exec pg_origin createdb --user postgres school_db
sudo docker exec pg_target createdb --user postgres school_db

sudo docker exec pg_origin createdb --user postgres physics_db
sudo docker exec pg_target createdb --user postgres physics_db

# Only do this block if we are populating school_db from scratch
SCHOOL_DB_DUMP_URL="https://s3.amazonaws.com/db-subsetter/load-test/school-db/pgdump.sql.gz"
wget -q -O - "${SCHOOL_DB_DUMP_URL}" | gunzip | sudo docker exec --interactive pg_origin psql --user postgres --dbname school_db

# Only do this block if we are populating physics_db from scratch
wget -O /home/ubuntu/tmp-data/physics-db-dump.tar "https://s3.amazonaws.com/db-subsetter/load-test/physics-db/physics-db-dump.tar"
tar -xvf /home/ubuntu/tmp-data/physics-db-dump.tar --directory /home/ubuntu/tmp-data/
sudo docker exec pg_origin pg_restore --jobs 8 --user postgres --dbname physics_db --verbose /tmp-data/physics-db-dump

sudo docker exec pg_origin psql --user postgres --dbname school_db -c "VACUUM ANALYZE"
sudo docker exec pg_origin psql --user postgres --dbname physics_db -c "VACUUM ANALYZE"

sudo docker exec pg_origin pg_dump --user postgres --dbname school_db --section pre-data | \
  sudo docker exec --interactive pg_target psql --user postgres --dbname school_db

sudo docker exec pg_origin pg_dump --user postgres --dbname physics_db --section pre-data | \
  sudo docker exec --interactive pg_target psql --user postgres --dbname physics_db
