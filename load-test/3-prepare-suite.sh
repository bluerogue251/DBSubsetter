#!/usr/bin/env bash

set -eou pipefail

# Assumes the postgres data is preloaded into a volume mounted at /pg-origin-data
# $ lsblk or $ sudo fdisk -l # --> see what it's called and substitute into next command, maybe /dev/xvdb, maybe /dev/nvme0n1, etc.
sudo mount /dev/nvme0n1 /pg-origin-data


# Ensure that the instance is in an availability zone for which the EBS Snapshot has "Fast Snapshot Restores" enabled (us-east-1f)
# https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/ebs-fast-snapshot-restore.html
# If not, then initialize the EBS Volume
# See https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/ebs-initialize.html
# sudo apt-get install -y fio
# sudo fio --filename=/dev/nvme0n1 --rw=read --bs=128k --iodepth=32 --ioengine=libaio --direct=1 --name=volume-initialize


sudo docker start pg_origin
sudo docker start pg_target
sudo docker start prometheus
