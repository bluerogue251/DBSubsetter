#!/usr/bin/env bash

set -e

pg_dump -h localhost -p 5520 -U postgres --section=post-data self_referencing_origin | psql -h localhost -p 5521 -U postgres self_referencing_target -v ON_ERROR_STOP=1