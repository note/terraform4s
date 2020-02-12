terraform {
  required_version = ">= 0.12.0"
}

provider "aws" {
  profile = var.aws_profile_name
  region = "eu-central-1"
  version = "~> 2.43.0"
}

resource "aws_kinesis_stream" "example_stream" {
  name             = "terraform4s-example"
  shard_count      = 1
  retention_period = 24

  shard_level_metrics = [
    "IncomingBytes",
    "OutgoingBytes",
  ]

  tags = {
    Environment = "test"
  }
}

resource "aws_s3_bucket" "example_bucket" {
  bucket = "${aws_kinesis_stream.example_stream.name}-bucket"
  acl    = "private"

  tags = {
    Environment = "test"
  }
}
