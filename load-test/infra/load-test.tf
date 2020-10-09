terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "3.9.0"
    }
  }
}

provider "aws" {
  profile = "default"
  region  = "us-west-2"
}

// $ ssh-keygen -f load-test -C "theodore.widom@gmail.com"
resource "aws_key_pair" "load-test" {
  key_name   = "woot"
  public_key = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAACAQDcZvp1JKifuuByKT1PGnjD01f2NcJoq4q8VyamLmHB+ynwegzygER53FiHy7GTMPradrFlTUVZnbgYlU04bxKaTsZ8Tyc8oj3m5/rdiDfwkoQXhh4iDiZl/bKotT0DjylDsuo0eAqdUJD2EkZweexm0EmaBckwSjzprNJ2p88Cum8B8c5t02Xh3xYYjioEH2k7eYQoU4vqOq2gAPN2Gr8NANwkef/RvWYNX7LUWOXN5Gmu203y17DKgIlGRULFv4NO/IcoMcbx5HwwQwAiw9PRx1DMYQPc20Ogoa15D8rrJgAIxeRqQlkAxyXY8cZm/i6zpPthLbbg/z02s20xxmH+ni+hF5eqhu7TT3gOOKvlWTYx5Io4AkJeIA8ny7IATD3N8iq0IowfWF/OpOmPiRG78q+ABd+IQo6W4U7mbgYwXB/NJTCmU81v3GJbp4YC5U9SxfwTKMDmsnRFM7RHf6Tvyjt4cMhLkwLVx2xMJEcN5zp6+a5FLU7DJnfD/aNdem2NgGAnrj/eCtfviQzF1wcfBuaakTzD4c8gJZZLsbln7+RDqO7uvuhyp+cBafZFVpWlp6hbHxN3BUdPWJVEOXDgHs6yC1VkG4Wd9QkO9BXowgnuOSxnI8mvlcaiw7FGZStTfnvXhhLVhHz9k/dpL82uWLqh6AnEQOEcsVz4wp1Ufw== theodore.widom@gmail.com"
}

// t3.medium   2 vCPU   4 GB RAM
// t3.xlarge   4 vCPU   16 GB RAM
resource "aws_instance" "control-panel" {
  availability_zone = "us-west-2c"
  ami           = "ami-06e54d05255faf8f6"
  instance_type = "t3.medium"
  key_name = "load-test"
  ebs_optimized = false
  monitoring = false

  ebs_block_device {
    device_name = "/dev/sdb"
    volume_type = "gp2"
    volume_size = 100
    delete_on_termination = true
    encrypted = false
  }
}

resource "aws_instance" "pg-origin" {
  availability_zone = "us-west-2c"
  ami = "ami-06e54d05255faf8f6"
  instance_type = "t3.xlarge"
  ebs_optimized = true
  monitoring = false

  ebs_block_device {
    device_name = "/dev/sdb"
    volume_type = "gp2"
    volume_size = 200
    delete_on_termination = true
    encrypted = false
  }
}

resource "aws_instance" "pg-target" {
  availability_zone = "us-west-2c"
  ami = "ami-06e54d05255faf8f6"
  instance_type = "t3.medium"
  ebs_optimized = true
  monitoring = false

  ebs_block_device {
    device_name = "/dev/sdb"
    volume_type = "gp2"
    volume_size = 100
    delete_on_termination = true
    encrypted = false
  }
}
