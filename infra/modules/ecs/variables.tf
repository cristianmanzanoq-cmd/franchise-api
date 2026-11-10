variable "name_prefix" {
  description = "Prefix for resource names"
  type        = string
}

variable "aws_region" {
  description = "AWS region, used for CloudWatch log configuration"
  type        = string
}

variable "vpc_id" {
  description = "VPC where the service runs"
  type        = string
}

variable "private_subnet_ids" {
  description = "Private subnets for the ECS tasks"
  type        = list(string)
}

variable "alb_security_group_id" {
  description = "Security group of the ALB allowed to reach the tasks"
  type        = string
}

variable "target_group_arn" {
  description = "Target group that receives traffic from the ALB"
  type        = string
}

variable "container_image" {
  description = "Full image reference (ECR URL + tag)"
  type        = string
}

variable "container_port" {
  description = "Port exposed by the container"
  type        = number
}

variable "task_cpu" {
  description = "Fargate task CPU units"
  type        = number
}

variable "task_memory" {
  description = "Fargate task memory in MiB"
  type        = number
}

variable "desired_count" {
  description = "Initial number of tasks"
  type        = number
}

variable "enable_autoscaling" {
  description = "Create Application Auto Scaling resources for the ECS service"
  type        = bool
  default     = false
}

variable "min_capacity" {
  description = "Minimum tasks for auto scaling"
  type        = number
}

variable "max_capacity" {
  description = "Maximum tasks for auto scaling"
  type        = number
}

variable "mongodb_uri_secret_arn" {
  description = "ARN of the Secrets Manager secret with the MongoDB URI"
  type        = string
}

variable "ecr_repository_arn" {
  description = "ARN of the ECR repository the execution role may pull from"
  type        = string
}
