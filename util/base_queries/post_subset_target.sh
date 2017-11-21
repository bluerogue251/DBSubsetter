#!/usr/bin/env bash

set -e

pg_dump -h localhost -p 5510 -U postgres --section=post-data base_queries_origin | psql -h localhost -p 5511 -U postgres base_queries_target -v ON_ERROR_STOP=1