#!/usr/bin/env bash

set -eou pipefail

# Run infrequently to prepare base load testing infrastructure.

# Install docker (in the future try to lock this down to version 18.06.1-ce)
sudo snap install docker

# Install JRE
sudo add-apt-repository -y ppa:openjdk-r/ppa
sudo apt install -y openjdk-8-jre-headless

# Create permanent origin data directory
mkdir --parents /home/ubuntu/pgdata/origin

# Create temporary utility directory
mkdir --parents /home/ubuntu/tmp-data

# Load postgres origin data onto disk using a temporary docker container
sudo docker run \
  --detach \
  --name tmp \
  --volume /home/ubuntu/pgdata/origin:/var/lib/postgresql/data \
  --volume /home/ubuntu/tmp-data:/tmp-data \
  -p 5432:5432 \
  postgres:9.6.3 \
  postgres -c 'max_connections=15'

# Wait for postgres to be available
sleep 10

sudo docker exec tmp createdb --user postgres school_db
sudo docker exec tmp createdb --user postgres physics_db

SCHOOL_DB_DUMP_URL="https://s3.amazonaws.com/db-subsetter/load-test/school-db/pgdump.sql.gz"
wget -q -O - "${SCHOOL_DB_DUMP_URL}" | gunzip | sudo docker exec --interactive tmp psql --user postgres --dbname school_db

wget -O /home/ubuntu/tmp-data/physics-db-dump.tar "https://s3.amazonaws.com/db-subsetter/load-test/physics-db/physics-db-dump.tar"
tar -xvf /home/ubuntu/tmp-data/physics-db-dump.tar --directory /home/ubuntu/tmp-data/
sudo docker exec --detach tmp pg_restore --jobs 4 --user postgres --dbname physics_db /tmp-data/physics-db-dump

# Can inspect progress with:
# sudo docker exec tmp psql --user postgres -c "select state, query from pg_stat_activity where state != 'idle'"
# df -h

# Clean up temporary utilities, leaving the postgres data permanently on disk
# sudo docker rm --force --volumes tmp
# rm -rf /home/ubuntu/tmp-data