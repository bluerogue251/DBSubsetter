#!/usr/bin/env bash


# Single Threaded Target
psql --host 0.0.0.0 --port 5573 --user postgres --dbname physics -c 'copy particle_domain to stdout' | psql --host 0.0.0.0 --port 5574 --user postgres --dbname physics -c 'copy particle_domain from stdin'
psql --host 0.0.0.0 --port 5573 --user postgres --dbname physics -c 'copy quantum_domain to stdout' | psql --host 0.0.0.0 --port 5574 --user postgres --dbname physics -c 'copy quantum_domain from stdin'
psql --host 0.0.0.0 --port 5573 --user postgres --dbname physics -c 'copy gravitational_wave_domain to stdout' | psql --host 0.0.0.0 --port 5574 --user postgres --dbname physics -c 'copy gravitational_wave_domain from stdin'

# Akka Streams Target
psql --host 0.0.0.0 --port 5573 --user postgres --dbname physics -c 'copy particle_domain to stdout' | psql --host 0.0.0.0 --port 5575 --user postgres --dbname physics -c 'copy particle_domain from stdin'
psql --host 0.0.0.0 --port 5573 --user postgres --dbname physics -c 'copy quantum_domain to stdout' | psql --host 0.0.0.0 --port 5575 --user postgres --dbname physics -c 'copy quantum_domain from stdin'
psql --host 0.0.0.0 --port 5573 --user postgres --dbname physics -c 'copy gravitational_wave_domain to stdout' | psql --host 0.0.0.0 --port 5575 --user postgres --dbname physics -c 'copy gravitational_wave_domain from stdin'
