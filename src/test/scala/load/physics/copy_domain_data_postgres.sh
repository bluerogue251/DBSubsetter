#!/usr/bin/env bash


origin_container=$1
origin_database=$2
target_container=$3
target_database=$4

docker exec ${origin_container} psql --user postgres --dbname ${origin_database} -c 'copy particle_domain to stdout' | docker exec -i ${target_container} psql --user postgres --dbname ${target_database} -c 'copy particle_domain from stdin'
docker exec ${origin_container} psql --user postgres --dbname ${origin_database} -c 'copy quantum_domain to stdout' | docker exec -i ${target_container} psql --user postgres --dbname ${target_database} -c 'copy quantum_domain from stdin'
docker exec ${origin_container} psql --user postgres --dbname ${origin_database} -c 'copy gravitational_wave_domain to stdout' | docker exec -i ${target_container} psql --user postgres --dbname ${target_database} -c 'copy gravitational_wave_domain from stdin'