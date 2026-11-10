variable "project_name" {
  description = "Project name used as prefix for every resource"
  type        = string
  default     = "franchise-api"
}

variable "aws_region" {
  description = "Region used by the simulated AWS environment"
  type        = string
  default     = "us-east-1"
}

variable "ministack_endpoint" {
  description = "MiniStack endpoint that emulates the AWS APIs"
  type        = string
  default     = "http://localhost:4566"
}

variable "vpc_cidr" {
  description = "CIDR block for the VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "availability_zones" {
  description = "Availability zones used for subnets"
  type        = list(string)
  default     = ["us-east-1a", "us-east-1b"]
}

variable "container_port" {
  description = "Port exposed by the application container"
  type        = number
  default     = 8080
}

variable "task_cpu" {
  description = "Fargate task CPU units"
  type        = number
  default     = 512
}

variable "task_memory" {
  description = "Fargate task memory in MiB"
  type        = number
  default     = 1024
}

variable "desired_count" {
  description = "Initial number of ECS tasks"
  type        = number
  default     = 2
}

variable "enable_autoscaling" {
  description = "Enable Application Auto Scaling (not implemented by MiniStack)"
  type        = bool
  default     = false
}

variable "min_capacity" {
  description = "Minimum number of ECS tasks for auto scaling"
  type        = number
  default     = 2
}

variable "max_capacity" {
  description = "Maximum number of ECS tasks for auto scaling"
  type        = number
  default     = 6
}

variable "image_tag" {
  description = "Docker image tag deployed to ECS"
  type        = string
  default     = "latest"
}

# MiniStack only starts ECS containers when the image reference exists in the
# host Docker daemon, so local runs point this at localhost:4566/<repo>:<tag>.
variable "container_image_override" {
  description = "Full image reference to deploy instead of the ECR URL (empty = ECR)"
  type        = string
  default     = ""
}

variable "mongodb_uri" {
  description = "MongoDB Atlas connection string, stored in Secrets Manager"
  type        = string
  sensitive   = true
}
