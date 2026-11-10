aws_region    = "us-east-1"
desired_count = 1
min_capacity  = 1
max_capacity  = 2
task_cpu      = 256
task_memory   = 512

# MiniStack does not implement Application Auto Scaling for ECS.
enable_autoscaling = false

# MiniStack starts tasks only from images present in the host Docker daemon.
# Build and tag locally, then push: docker push localhost:4566/franchise-api-dev:latest
container_image_override = "localhost:4566/franchise-api-dev:latest"

# mongodb_uri is provided via -var or TF_VAR_mongodb_uri, never committed.
