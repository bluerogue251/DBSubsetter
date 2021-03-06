#!/usr/bin/env bash

set -eou pipefail

# https://stackoverflow.com/questions/59895/how-to-get-the-source-directory-of-a-bash-script-from-within-the-script-itself
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

docker run \
  --rm \
  --volume "$DIR":/tf \
  --workdir /tf \
  hashicorp/terraform:0.13.4 \
  init
