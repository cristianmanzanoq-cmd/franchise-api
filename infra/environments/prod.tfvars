aws_region         = "us-east-1"
desired_count      = 3
min_capacity       = 3
max_capacity       = 10
task_cpu           = 1024
task_memory        = 2048
enable_autoscaling = true
# mongodb_uri is provided via -var or TF_VAR_mongodb_uri, never committed.
