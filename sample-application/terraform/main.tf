terraform {
  required_version = ">= 1.0.11"
}

provider "aws" {
  profile = var.aws_profile_name
  region = "eu-central-1"
  version = "~> 4.3.0"
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

  tags = {
    Environment = "test"
  }
}

resource "aws_s3_bucket_acl" "example_bucket_acl" {
  bucket = aws_s3_bucket.example_bucket.id
  acl    = "private"
}
