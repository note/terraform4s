terraform {
  required_version = ">= 0.12.0"
}

provider "aws" {
  region = "eu-central-1"
  version = "~> 2.43.0"
}
