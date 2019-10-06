#!/usr/bin/env bash

set -eou pipefail

# Runs before each load test -- maybe some more of this could be moved into the "AMI" itself

# Install docker (in the future try to lock this down to version 18.06.1-ce)
sudo apt-get update
sudo apt-get install -y docker.io

# Install JRE
sudo add-apt-repository -y ppa:openjdk-r/ppa
sudo apt install -y openjdk-8-jre-headless

# Setup temporary utility directory
mkdir tmp-data

# Mount the external volume
sudo mkdir /vol
sudo mount /dev/xvdf1 /vol -t ext4

# Assumes that the origin data is already located in /home/ubuntu/pg-origin-data
sudo docker create \
  --name pg_origin \
  --volume /home/ubuntu/pg-origin-data:/var/lib/postgresql/data \
  --volume /home/ubuntu/tmp-data:/tmp-data \
  -p 5432:5432 \
  postgres:9.6.3 \
  postgres -c 'max_connections=15' -c 'maintenance_work_mem=3GB'

sudo docker create \
  --name pg_target \
  --volume /home/ubuntu/tmp-data:/tmp-data \
  -p 5433:5432 \
  postgres:9.6.3 \
  postgres -c 'max_connections=15'

# TODO Here we currently manually upload prometheus-config.yml
mkdir --mode 777 prometheus-data
sudo docker create \
  --name prometheus \
  --volume /home/ubuntu/prometheus-config.yml:/etc/prometheus/prometheus.yml \
  --volume /home/ubuntu/prometheus-data:/prometheus \
  -p 9090:9090 \
  prom/prometheus:v2.6.0 \
  --web.enable-admin-api

sudo docker start pg_origin
sudo docker exec pg_origin psql --user postgres --dbname school_db -c "VACUUM ANALYZE"
nohup sudo docker exec pg_origin psql --user postgres --dbname physics_db -c "VACUUM ANALYZE" &
docker stop pg_origin