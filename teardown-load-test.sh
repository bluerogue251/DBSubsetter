#!/bin/bash

set -eou pipefail

echo "Spinning down AWS infrastructure"
./load-test/infra/destroy.sh
