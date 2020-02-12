#!/bin/bash

cd terraform
terraform init && terraform apply

aws dynamodb delete-table --table-name terraform4s-example --profile michal

