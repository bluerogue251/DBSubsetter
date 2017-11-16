#!/usr/bin/env bash

set -e

pg_dump -h localhost -p 5480 -U postgres --section=post-data circular_dep_origin | psql -h localhost -p 5481 -U postgres circular_dep_target -v ON_ERROR_STOP=1