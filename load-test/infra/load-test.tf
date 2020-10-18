terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "3.9.0"
    }
    template = {
      source = "hashicorp/template"
      version = "2.2.0"
    }
  }
}

data "template_file" "ssh-public-key" {
  template = file("./load-test.pem.pub")
}

data "template_file" "provision-origin" {
  template = file("./provision-origin.sh")
}

data "template_file" "provision-target" {
  template = file("./provision-target.sh")
  vars = {
    pg-origin-ip = aws_instance.pg-origin.private_ip
  }
}

data "template_file" "provision-monitor" {
  template = file("./provision-monitor.sh")
  vars = {
    pg-target-ip = aws_instance.pg-target.private_ip
  }
}

provider "aws" {
  profile = "default"
  region  = "us-east-1"
}

// $ ssh-keygen -f load-test -C "theodore.widom@gmail.com"
resource "aws_key_pair" "load-test" {
  key_name   = "load-test"
  public_key = data.template_file.ssh-public-key.rendered
}

// t3.medium   2 vCPU   4 GB RAM
// t3.xlarge   4 vCPU   16 GB RAM

resource "aws_instance" "pg-origin" {
  ami = "ami-0dba2cb6798deb6d8"
  instance_type = "t3.xlarge"
  key_name = "load-test"
  ebs_optimized = true
  monitoring = true
  user_data = data.template_file.provision-origin.rendered

  ebs_block_device {
    device_name = "/dev/sdb"
    volume_type = "gp2"
    volume_size = 400
    delete_on_termination = true
    encrypted = false
  }

  tags = {
    Name = "pg-origin"
  }
}

resource "aws_instance" "pg-target" {
  ami = "ami-0dba2cb6798deb6d8"
  instance_type = "t3.medium"
  key_name = "load-test"
  ebs_optimized = true
  monitoring = true
  user_data = data.template_file.provision-target.rendered

  ebs_block_device {
    device_name = "/dev/sdb"
    volume_type = "gp2"
    volume_size = 100
    delete_on_termination = true
    encrypted = false
  }

  tags = {
    Name = "pg-target"
  }
}

resource "aws_instance" "monitor" {
  ami           = "ami-0dba2cb6798deb6d8"
  instance_type = "t3.medium"
  key_name = "load-test"
  ebs_optimized = false
  monitoring = true
  user_data = data.template_file.provision-monitor.rendered

  ebs_block_device {
    device_name = "/dev/sdb"
    volume_type = "gp2"
    volume_size = 50
    delete_on_termination = true
    encrypted = false
  }

  tags = {
    Name = "monitor"
  }
}