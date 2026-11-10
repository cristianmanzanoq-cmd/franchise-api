# The MongoDB connection string never lives in code or plain environment
# variables: it is stored in Secrets Manager and injected by ECS at runtime.
resource "aws_secretsmanager_secret" "mongodb_uri" {
  name                    = "${var.name_prefix}/mongodb-uri"
  description             = "MongoDB Atlas connection string for the franchise API"
  recovery_window_in_days = 7
}

resource "aws_secretsmanager_secret_version" "mongodb_uri" {
  secret_id     = aws_secretsmanager_secret.mongodb_uri.id
  secret_string = var.mongodb_uri
}
