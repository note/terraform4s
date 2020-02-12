#!/bin/bash

# Exit on any failure
set -e

sbt "codegen/run newSbtProject --sbt-org-name pl.msitko --sbt-project-name terraform4s-aws --sbt-project-version 0.1.0 --provider-name aws --provider-version 2.43.0 --sbt-project-path test-out --out-package-name pl.msitko"

cp -r test-helpers/scala test-out/src/main/scala
cp -r test-helpers/terraform/*.tf test-out/

cd test-out

# This class declares optionalFields which conflicts with syntethic terraform4s field
# It's a hack and codegen should handle it in future
rm src/main/scala/pl/msitko/aws/AwsS3BucketInventory.scala

sbt run

# terraform init && terraform apply