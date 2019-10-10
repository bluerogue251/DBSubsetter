#!/usr/bin/env bash

set -eou pipefail


# Clean up temporary utilities from the previous step
sudo docker rm --force --volumes tmp
rm /home/ubuntu/tmp-data/physics-db-dump.tar
rm -rf /home/ubuntu/tmp-data/physics-db-dump

# Install JRE
sudo add-apt-repository -y ppa:openjdk-r/ppa
sudo apt install -y openjdk-8-jre-headless

# Assumes the postgres data is preloaded into a volume mounted at /pg-origin-data
# lsblk # --> see what it's called and substitute into next command, maybe /dev/xvdb, maybe /dev/xvdf1, etc.
# sudo mount /dev/xvdb /pg-origin-data
sudo chown -R ubuntu /pg-origin-data
sudo chmod --recursive 777 /pg-origin-data
sudo docker run \
  --detach \
  --name pg_origin \
  --volume /pg-origin-data:/var/lib/postgresql/data \
  --volume /home/ubuntu/tmp-data:/tmp-data \
  -p 5432:5432 \
  postgres:9.6.3 \
  postgres -c 'max_connections=15'

sudo docker create \
  --name pg_target \
  --volume /home/ubuntu/tmp-data:/tmp-data \
  -p 5433:5432 \
  postgres:9.6.3 \
  postgres -c 'max_connections=15' -c 'maintenance_work_mem=3GB'

# TODO Here we currently manually upload prometheus-config.yml
mkdir --mode 777 prometheus-data
sudo docker create \
  --name prometheus \
  --network host \
  --volume /home/ubuntu/prometheus-config.yml:/etc/prometheus/prometheus.yml \
  --volume /home/ubuntu/prometheus-data:/prometheus \
  prom/prometheus:v2.6.0 \
  --web.enable-admin-api

sudo docker exec pg_origin psql --user postgres --dbname school_db -c "VACUUM ANALYZE"
nohup sudo docker exec pg_origin psql --user postgres --dbname physics_db -c "VACUUM ANALYZE" && sudo docker stop pg_origin &

# And now wait for the physics_db VACUUM ANALYZE to complete in the background, about 30 mins