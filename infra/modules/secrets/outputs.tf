output "mongodb_uri_secret_arn" {
  description = "ARN of the secret holding the MongoDB URI"
  value       = aws_secretsmanager_secret.mongodb_uri.arn
}
