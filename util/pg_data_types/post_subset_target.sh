#!/usr/bin/env bash

set -e

pg_dump -h localhost -p 5500 -U postgres --section=post-data pg_data_types_origin | psql -h localhost -p 5501 -U postgres pg_data_types_target -v ON_ERROR_STOP=1