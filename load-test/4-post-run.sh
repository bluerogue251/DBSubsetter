#!/usr/bin/env bash

set -eou pipefail

# Runs after each load test

sudo docker exec pg_origin pg_dump --user postgres --dbname school_db --section post-data | \
  sudo docker exec --interactive pg_target psql --user postgres --dbname school_db

sudo docker exec pg_origin pg_dump --user postgres --dbname physics_db --section post-data | \
  sudo docker exec --interactive pg_target psql --user postgres --dbname physics_db
