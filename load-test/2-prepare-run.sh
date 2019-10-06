#!/usr/bin/env bash

set -eou pipefail

# Runs before each load test

# Assumes that the origin data is already located in /home/ubuntu/pgdata/origin
sudo docker run \
  --detach \
  --name pg_origin \
  --volume /home/ubuntu/pgdata/origin:/var/lib/postgresql/data \
  -p 5432:5432 \
  postgres:9.6.3 \
  postgres -c 'max_connections=15'

sudo docker run \
  --detach \
  --name pg_target \
  -p 5432:5432 \
  postgres:9.6.3 \
  postgres -c 'max_connections=15'

sudo docker run \
  --detach \
  --name prometheus \
  --volume /home/ubuntu/prometheus-config.yml:/etc/prometheus/prometheus.yml \
  prom/prometheus:v2.6.0

sudo docker exec pg_target createdb --user postgres school_db
sudo docker exec pg_target createdb --user postgres physics_db

sudo docker exec pg_origin pg_dump --user postgres --dbname school_db --section pre-data | \
  sudo docker exec --interactive pg_target psql --user postgres --dbname school_db

sudo docker exec pg_origin pg_dump --user postgres --dbname physics_db --section pre-data | \
  sudo docker exec --interactive pg_target psql --user postgres --dbname physics_db

sudo docker exec pg_origin psql --user postgres --dbname school_db -c "VACUUM ANALYZE"
sudo docker exec pg_origin psql --user postgres --dbname physics_db -c "VACUUM ANALYZE"
sudo docker exec pg_target psql --user postgres --dbname school_db -c "VACUUM ANALYZE"
sudo docker exec pg_target psql --user postgres --dbname physics_db -c "VACUUM ANALYZE"
