#!/usr/bin/env bash

set -eou pipefail

# Runs before each load test -- maybe some more of this could be moved into the "AMI" itself

# Install docker (in the future try to lock this down to version 18.06.1-ce)
sudo apt-get install -y docker.io

# Install JRE
sudo add-apt-repository -y ppa:openjdk-r/ppa
sudo apt install -y openjdk-8-jre-headless

# Mount the external volume
sudo mkdir /vol
sudo mount /dev/xvdf1 /vol -t ext4
# Necessary?
# sudo chmod -R 777 /vol

# Assumes that the origin data is already located in /vol/home/ubuntu/pgdata/origin
sudo docker run \
  --detach \
  --name pg_origin \
  --volume /vol/home/ubuntu/pgdata/origin:/var/lib/postgresql/data \
  -p 5432:5432 \
  postgres:9.6.3 \
  postgres -c 'max_connections=15'

sudo docker run \
  --detach \
  --name pg_target \
  -p 5433:5432 \
  postgres:9.6.3 \
  postgres -c 'max_connections=15'

# TODO upload prometheus-config.yml
sudo docker run \
  --detach \
  --name prometheus \
  --volume /home/ubuntu/prometheus-config.yml:/etc/prometheus/prometheus.yml \
  -p 9090:9090 \
  prom/prometheus:v2.6.0

sudo docker exec pg_target createdb --user postgres school_db
sudo docker exec pg_target createdb --user postgres physics_db

sudo docker exec pg_origin pg_dump --user postgres --dbname school_db --section pre-data | \
  sudo docker exec --interactive pg_target psql --user postgres --dbname school_db

sudo docker exec pg_origin pg_dump --user postgres --dbname physics_db --section pre-data | \
  sudo docker exec --interactive pg_target psql --user postgres --dbname physics_db

sudo docker exec pg_origin psql --user postgres --dbname school_db -c "VACUUM ANALYZE"
sudo docker exec pg_origin psql --user postgres --dbname physics_db -c "VACUUM ANALYZE"

DB_SUBSETTER_JAR_URL="https://s3.amazonaws.com/db-subsetter/load-test/jars/DBSubsetter-assembly-f91e64d0d622aeebf44f217e365f35ac990fd534.jar"
wget -O DBSubsetter.jar "${DB_SUBSETTER_JAR_URL}"
