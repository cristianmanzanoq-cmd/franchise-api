# Remote state in the MiniStack S3 bucket with DynamoDB locking. Backend blocks
# cannot read variables, so the endpoint is literal. Credentials come from the
# AWS_ACCESS_KEY_ID / AWS_SECRET_ACCESS_KEY environment variables.
# Workspaces (dev/staging/prod) get isolated state under env:/<workspace>/.
terraform {
  backend "s3" {
    bucket         = "franchise-api-terraform-state"
    key            = "franchise-api/terraform.tfstate"
    region         = "us-east-1"
    dynamodb_table = "franchise-api-terraform-locks"
    encrypt        = true

    force_path_style            = true
    skip_credentials_validation = true
    skip_metadata_api_check     = true
    skip_requesting_account_id  = true

    endpoints = {
      s3       = "http://localhost:4566"
      dynamodb = "http://localhost:4566"
    }
  }
}
