terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "3.9.0"
    }
  }
}

data "template_file" "provision-postgres" {
  template = file("./provision-postgres.sh")
}

provider "aws" {
  profile = "default"
  region  = "us-east-1"
}

// $ ssh-keygen -f load-test -C "theodore.widom@gmail.com"
resource "aws_key_pair" "load-test" {
  key_name   = "load-test"
  public_key = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABgQCfb5w6ksmww9O0Yfr6n7bKG1oTmaQLrlehVC9BD8WEA/BBbteTSBn57d/fqGBrfJtnTxOCZe/1ZBKg+ZrPlSXus8lndJXMkp6D08caGytHpd/VapvA1JP8LJiq5upIfpFdBjxx5Fw8Fg0TpJSmkvSChoKsjEjbrCCPprJTVcbRsKci9EHqARRM/Iae1DWsvDTmSaMOfGgKdTJRNE4yE+IFD/5HxqJnJfKKjXSCFZJyCPNVVzh/OsMju+tar9GGQNQRhIeELm0ef4esy6WOxFlGs7D9ylyb+OVgoXKauDGms/U442B6eKvFFkJaXyAl2nIf1BOOipw3PDHddmvO1P6ppBEAAHWmQZFJwluByvMuJCF7TT/5Ocpm2fEEJdxN3Fd3HseoYaDzz2vtYvJwSLJeUvwB6T56AWEkxdA8Jl/oX4j6u8uOsuNKizqP04DleWzyWXMk03je44GVK8Vyt1ykO3xy0GNS3aiYotNjn5tW8Y9vGJQZLuZiazVa6yC9nFs= theodore.widom@gmail.com"
}

// t3.medium   2 vCPU   4 GB RAM
// t3.xlarge   4 vCPU   16 GB RAM
resource "aws_instance" "control-panel" {
  ami           = "ami-0dba2cb6798deb6d8"
  instance_type = "t3.medium"
  key_name = "load-test"
  ebs_optimized = false
  monitoring = true

  ebs_block_device {
    device_name = "/dev/sdb"
    volume_type = "gp2"
    volume_size = 50
    delete_on_termination = true
    encrypted = false
  }
}

resource "aws_instance" "pg-origin" {
  ami = "ami-0dba2cb6798deb6d8"
  instance_type = "t3.xlarge"
  key_name = "load-test"
  ebs_optimized = true
  monitoring = true
  user_data = data.template_file.provision-postgres.rendered

  ebs_block_device {
    device_name = "/dev/sdb"
    volume_type = "gp2"
    volume_size = 200
    delete_on_termination = true
    encrypted = false
  }
}

resource "aws_instance" "pg-target" {
  ami = "ami-0dba2cb6798deb6d8"
  instance_type = "t3.medium"
  key_name = "load-test"
  ebs_optimized = true
  monitoring = true

  ebs_block_device {
    device_name = "/dev/sdb"
    volume_type = "gp2"
    volume_size = 100
    delete_on_termination = true
    encrypted = false
  }
}
