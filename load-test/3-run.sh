#!/usr/bin/env bash

set -eou pipefail

echo "Run"

# Export prometheus metrics
# curl -X POST http://localhost:9090/api/v1/admin/tsdb/snapshot
# {
#  "status": "success",
#  "data": {
#    "name": "20171210T211224Z-2be650b6d019eb54"
#  }
#}
# The snapshot now exists at <data-dir>/snapshots/20171210T211224Z-2be650b6d019eb54