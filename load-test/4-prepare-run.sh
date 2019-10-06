#!/usr/bin/env bash

set -eou pipefail


sudo docker exec pg_target dropdb --user postgres --if-exists school_db
sudo docker exec pg_target dropdb --user postgres --if-exists physics_db
sudo docker exec pg_target createdb --user postgres school_db
sudo docker exec pg_target createdb --user postgres physics_db

sudo docker exec pg_origin pg_dump --user postgres --dbname school_db --section pre-data | \
  sudo docker exec --interactive pg_target psql --user postgres --dbname school_db

sudo docker exec pg_origin pg_dump --user postgres --dbname physics_db --section pre-data | \
  sudo docker exec --interactive pg_target psql --user postgres --dbname physics_db
