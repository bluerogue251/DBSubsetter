#!/usr/bin/env bash


origin_container=$1
target_container=$2

docker exec ${origin_container} psql --user postgres --dbname physics -c 'copy particle_domain to stdout' | docker exec -i ${target_container} psql --user postgres --dbname physics -c 'copy particle_domain from stdin'
docker exec ${origin_container} psql --user postgres --dbname physics -c 'copy quantum_domain to stdout' | docker exec -i ${target_container} psql --user postgres --dbname physics -c 'copy quantum_domain from stdin'
docker exec ${origin_container} psql --user postgres --dbname physics -c 'copy gravitational_wave_domain to stdout' | docker exec -i ${target_container} psql --user postgres --dbname physics -c 'copy gravitational_wave_domain from stdin'