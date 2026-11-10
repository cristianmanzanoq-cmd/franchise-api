variable "name_prefix" {
  description = "Prefix for resource names"
  type        = string
}

variable "mongodb_uri" {
  description = "MongoDB Atlas connection string"
  type        = string
  sensitive   = true
}
