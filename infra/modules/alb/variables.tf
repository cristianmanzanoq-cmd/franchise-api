variable "name_prefix" {
  description = "Prefix for resource names"
  type        = string
}

variable "vpc_id" {
  description = "VPC where the ALB is created"
  type        = string
}

variable "public_subnet_ids" {
  description = "Public subnets for the ALB"
  type        = list(string)
}

variable "container_port" {
  description = "Port exposed by the application container"
  type        = number
}
