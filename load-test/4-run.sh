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

echo "Running load test of school_db"
java -Xmx4G -jar DBSubsetter.jar \
  --originDbConnStr "jdbc:postgresql://0.0.0.0:5432/school_db?user=postgres" \
  --targetDbConnStr "jdbc:postgresql://0.0.0.0:5433/school_db?user=postgres" \
  --originDbParallelism 8 \
  --targetDbParallelism 8 \
  --schemas "school_db,Audit" \
  --baseQuery "school_db.Students ::: student_id % 25 = 0 ::: includeChildren" \
  --baseQuery "school_db.standalone_table ::: id < 4 ::: includeChildren" \
  --excludeColumns "school_db.schools(mascot)" \
  --excludeTable "school_db.empty_table_2" \
  --preTargetBufferSize 10000 \
  --exposeMetrics

# TODO: fix so that some experiment plans have no scientist. Then use this base query to test auto-skipPkStore calculations
# "--baseQuery", "public.experiment_plans ::: id % 35 = 0 ::: includeChildren",
echo "Running load test of physics_db"
nohup java -Xmx4G -jar DBSubsetter.jar \
  --originDbConnStr "jdbc:postgresql://0.0.0.0:5432/physics_db?user=postgres" \
  --targetDbConnStr "jdbc:postgresql://0.0.0.0:5433/physics_db?user=postgres" \
  --originDbParallelism 8 \
  --targetDbParallelism 8 \
  --schemas "public" \
  --baseQuery "public.scientists ::: id in (2) ::: includeChildren" \
  --excludeTable "public.particle_domain" \
  --excludeTable "public.quantum_domain" \
  --excludeTable "public.gravitational_wave_domain" \
  --skipPkStore "public.datum_note_responses" \
  --skipPkStore "public.datum_notes" \
  --skipPkStore "public.gravitational_wave_data" \
  --skipPkStore "public.particle_collider_data" \
  --skipPkStore "public.quantum_data" \
  --exposeMetrics &
