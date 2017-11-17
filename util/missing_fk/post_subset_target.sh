#!/usr/bin/env bash

set -e

pg_dump -h localhost -p 5490 -U postgres --section=post-data missing_fk_origin | psql -h localhost -p 5491 -U postgres missing_fk_target -v ON_ERROR_STOP=1