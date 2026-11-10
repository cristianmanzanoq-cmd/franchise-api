terraform {
  required_version = ">= 1.6.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.40"
    }
  }
}

provider "aws" {
  region = var.aws_region

  s3_use_path_style           = true
  skip_credentials_validation = true
  skip_metadata_api_check     = true
  skip_requesting_account_id  = true

  endpoints {
    applicationautoscaling = var.ministack_endpoint
    cloudwatch             = var.ministack_endpoint
    cloudwatchlogs         = var.ministack_endpoint
    ec2                    = var.ministack_endpoint
    ecr                    = var.ministack_endpoint
    ecs                    = var.ministack_endpoint
    elbv2                  = var.ministack_endpoint
    iam                    = var.ministack_endpoint
    secretsmanager         = var.ministack_endpoint
    sts                    = var.ministack_endpoint
  }

  default_tags {
    tags = {
      Project     = var.project_name
      Environment = terraform.workspace
      ManagedBy   = "terraform"
    }
  }
}
