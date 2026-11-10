locals {
  # Workspace-aware prefix: franchise-api-dev, franchise-api-staging, franchise-api-prod
  name_prefix = "${var.project_name}-${terraform.workspace}"
}

module "network" {
  source = "./modules/network"

  name_prefix        = local.name_prefix
  vpc_cidr           = var.vpc_cidr
  availability_zones = var.availability_zones
}

module "ecr" {
  source = "./modules/ecr"

  name_prefix = local.name_prefix
}

module "secrets" {
  source = "./modules/secrets"

  name_prefix = local.name_prefix
  mongodb_uri = var.mongodb_uri
}

module "alb" {
  source = "./modules/alb"

  name_prefix       = local.name_prefix
  vpc_id            = module.network.vpc_id
  public_subnet_ids = module.network.public_subnet_ids
  container_port    = var.container_port
}

module "ecs" {
  source = "./modules/ecs"

  name_prefix            = local.name_prefix
  aws_region             = var.aws_region
  vpc_id                 = module.network.vpc_id
  private_subnet_ids     = module.network.private_subnet_ids
  alb_security_group_id  = module.alb.alb_security_group_id
  target_group_arn       = module.alb.target_group_arn
  container_image        = var.container_image_override != "" ? var.container_image_override : "${module.ecr.repository_url}:${var.image_tag}"
  container_port         = var.container_port
  task_cpu               = var.task_cpu
  task_memory            = var.task_memory
  desired_count          = var.desired_count
  enable_autoscaling     = var.enable_autoscaling
  min_capacity           = var.min_capacity
  max_capacity           = var.max_capacity
  mongodb_uri_secret_arn = module.secrets.mongodb_uri_secret_arn
  ecr_repository_arn     = module.ecr.repository_arn
}
