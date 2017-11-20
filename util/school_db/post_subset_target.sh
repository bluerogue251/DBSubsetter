#!/usr/bin/env bash

set -e

pg_dump -h localhost -p 5450 -U postgres --section=post-data school_db_origin | psql -h localhost -p 5451 -U postgres school_db_target -v ON_ERROR_STOP=1