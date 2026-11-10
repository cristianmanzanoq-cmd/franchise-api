output "alb_dns_name" {
  description = "Public DNS of the load balancer (API entry point)"
  value       = module.alb.alb_dns_name
}

output "ecr_repository_url" {
  description = "ECR repository URL to push the Docker image"
  value       = module.ecr.repository_url
}

output "ecs_cluster_name" {
  description = "Name of the ECS cluster"
  value       = module.ecs.cluster_name
}

output "mongodb_uri_secret_arn" {
  description = "ARN of the Secrets Manager secret holding the MongoDB URI"
  value       = module.secrets.mongodb_uri_secret_arn
  sensitive   = true
}
