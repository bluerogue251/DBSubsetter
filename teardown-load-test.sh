#!/bin/bash

set -eou pipefail

echo "Spinning down load test infrastructure"
./load-test/infra/destroy.sh
