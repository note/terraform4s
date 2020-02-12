terraform {
  required_version = ">= 0.12.0"
}

provider "aws" {
  profile = var.aws_profile_name
  region = "eu-central-1"
  version = "~> 2.43.0"
}
