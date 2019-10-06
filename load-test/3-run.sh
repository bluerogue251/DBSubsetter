#!/usr/bin/env bash

set -eou pipefail

echo "Run"

echo "Running load test of school_db"
java -jar DBSubsetter.jar \
  --originDbConnStr "jdbc:postgresql://0.0.0.0:5432/school_db?user=postgres" \
  --targetDbConnStr "jdbc:postgresql://0.0.0.0:5433/school_db?user=postgres" \
  --originDbParallelism 8 \
  --targetDbParallelism 8 \
  --schemas "school_db,Audit" \
  --baseQuery "school_db.Students ::: student_id % 100 = 0 ::: includeChildren" \
  --baseQuery "school_db.standalone_table ::: id < 4 ::: includeChildren" \
  --excludeColumns "school_db.schools(mascot)" \
  --excludeTable "school_db.empty_table_2" \
  --preTargetBufferSize 10000 \
  --exposeMetrics

# TODO: fix so that some experiment plans have no scientist. Then use this base query to test auto-skipPkStore calculations
# "--baseQuery", "public.experiment_plans ::: id % 35 = 0 ::: includeChildren",
echo "Running load test of physics_db"
java -jar DBSubsetter.jar \
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
  --exposeMetrics

# Export prometheus metrics
# curl -X POST http://localhost:9090/api/v1/admin/tsdb/snapshot
# {
#  "status": "success",
#  "data": {
#    "name": "20171210T211224Z-2be650b6d019eb54"
#  }
#}
# The snapshot now exists at <data-dir>/snapshots/20171210T211224Z-2be650b6d019eb54