#!/usr/bin/env bash

set -eou pipefail

# Run infrequently to prepare base load testing infrastructure.

# Clean up temporary utilities from the previous step
sudo docker rm --force --volumes tmp
rm /home/ubuntu/tmp-data/physics-db-dump.tar
rm -rf /home/ubuntu/tmp-data/physics-db-dump

# Install JRE
sudo add-apt-repository -y ppa:openjdk-r/ppa
sudo apt install -y openjdk-8-jre-headless
