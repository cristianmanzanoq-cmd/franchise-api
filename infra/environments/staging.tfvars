aws_region         = "us-east-1"
desired_count      = 2
min_capacity       = 2
max_capacity       = 4
task_cpu           = 512
task_memory        = 1024
enable_autoscaling = true
# mongodb_uri is provided via -var or TF_VAR_mongodb_uri, never committed.
