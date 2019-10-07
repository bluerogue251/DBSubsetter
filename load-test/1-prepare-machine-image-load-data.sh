#!/usr/bin/env bash

set -eou pipefail


# Install docker
sudo apt-get update
sudo apt-get install -y docker.io

# Create utility directory
mkdir /home/ubuntu/tmp-data

# Load postgres origin data onto disk using a temporary docker container
# Assumes an existing external volume mounted at /pg-origin-data
# lsblk # --> see what it's called and substitute into next command, maybe /dev/xvdb, maybe /dev/xvdf1, etc.
# sudo mount /dev/xvdb /pg-origin-data
sudo docker run \
  --detach \
  --name tmp \
  --volume /pg-origin-data:/var/lib/postgresql/data \
  --volume /home/ubuntu/tmp-data:/tmp-data \
  -p 5432:5432 \
  postgres:9.6.3 \
  postgres -c 'max_connections=15' -c 'maintenance_work_mem=3GB' -c 'max_wal_size=4GB' -c 'full_page_writes=off' -c 'autovacuum=off' -c 'fsync=off'

# Wait for postgres to be available
sleep 10

sudo docker exec tmp createdb --user postgres school_db
sudo docker exec tmp createdb --user postgres physics_db

SCHOOL_DB_DUMP_URL="https://s3.amazonaws.com/db-subsetter/load-test/school-db/pgdump.sql.gz"
wget -q -O - "${SCHOOL_DB_DUMP_URL}" | gunzip | sudo docker exec --interactive tmp psql --user postgres --dbname school_db

wget -O /home/ubuntu/tmp-data/physics-db-dump.tar "https://s3.amazonaws.com/db-subsetter/load-test/physics-db/physics-db-dump.tar"
tar -xvf /home/ubuntu/tmp-data/physics-db-dump.tar --directory /home/ubuntu/tmp-data/
sudo docker exec --detach tmp pg_restore --jobs 4 --user postgres --dbname physics_db /tmp-data/physics-db-dump

# And now wait for the physics_db data to load in the background. This may take many, many hours.
# Can inspect progress with:
# sudo docker exec tmp psql --user postgres -c "select state, query from pg_stat_activity where state != 'idle'"
# And with:
# df -h