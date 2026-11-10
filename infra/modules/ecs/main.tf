resource "aws_ecs_cluster" "this" {
  name = "${var.name_prefix}-cluster"

  setting {
    name  = "containerInsights"
    value = "enabled"
  }
}

resource "aws_cloudwatch_log_group" "this" {
  name              = "/ecs/${var.name_prefix}"
  retention_in_days = 14
}

data "aws_iam_policy_document" "task_assume" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }
  }
}

# Execution role: used by the ECS agent to pull the image, write logs and
# read ONLY the MongoDB URI secret (least privilege: resource-scoped).
resource "aws_iam_role" "execution" {
  name               = "${var.name_prefix}-execution-role"
  assume_role_policy = data.aws_iam_policy_document.task_assume.json
}

data "aws_iam_policy_document" "execution" {
  statement {
    sid       = "EcrAuthorizationToken"
    actions   = ["ecr:GetAuthorizationToken"]
    resources = ["*"]
  }

  statement {
    sid = "PullImageFromRepository"
    actions = [
      "ecr:BatchCheckLayerAvailability",
      "ecr:GetDownloadUrlForLayer",
      "ecr:BatchGetImage"
    ]
    resources = [var.ecr_repository_arn]
  }

  statement {
    sid = "WriteLogs"
    actions = [
      "logs:CreateLogStream",
      "logs:PutLogEvents"
    ]
    resources = ["${aws_cloudwatch_log_group.this.arn}:*"]
  }

  statement {
    sid       = "ReadMongoSecretOnly"
    actions   = ["secretsmanager:GetSecretValue"]
    resources = [var.mongodb_uri_secret_arn]
  }
}

resource "aws_iam_role_policy" "execution" {
  name   = "${var.name_prefix}-execution-policy"
  role   = aws_iam_role.execution.id
  policy = data.aws_iam_policy_document.execution.json
}

# Task role: identity of the application itself. The API does not call any
# AWS service at runtime, so it gets no permissions at all (least privilege).
resource "aws_iam_role" "task" {
  name               = "${var.name_prefix}-task-role"
  assume_role_policy = data.aws_iam_policy_document.task_assume.json
}

resource "aws_ecs_task_definition" "this" {
  family                   = var.name_prefix
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = var.task_cpu
  memory                   = var.task_memory
  execution_role_arn       = aws_iam_role.execution.arn
  task_role_arn            = aws_iam_role.task.arn

  container_definitions = jsonencode([{
    name      = "franchise-api"
    image     = var.container_image
    essential = true

    portMappings = [{
      containerPort = var.container_port
      protocol      = "tcp"
    }]

    # The secret is resolved by ECS at task start and injected in memory;
    # it is never stored in the task definition in plain text.
    secrets = [{
      name      = "MONGODB_URI"
      valueFrom = var.mongodb_uri_secret_arn
    }]

    logConfiguration = {
      logDriver = "awslogs"
      options = {
        awslogs-group         = aws_cloudwatch_log_group.this.name
        awslogs-region        = var.aws_region
        awslogs-stream-prefix = "app"
      }
    }

    healthCheck = {
      command     = ["CMD-SHELL", "wget -qO- http://localhost:${var.container_port}/actuator/health || exit 1"]
      interval    = 30
      timeout     = 5
      retries     = 3
      startPeriod = 60
    }
  }])
}

resource "aws_security_group" "service" {
  name        = "${var.name_prefix}-service-sg"
  description = "Allow traffic only from the ALB"
  vpc_id      = var.vpc_id

  ingress {
    description     = "App port from ALB only"
    from_port       = var.container_port
    to_port         = var.container_port
    protocol        = "tcp"
    security_groups = [var.alb_security_group_id]
  }

  egress {
    description = "Outbound to ECR, Secrets Manager and MongoDB Atlas"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.name_prefix}-service-sg"
  }
}

resource "aws_ecs_service" "this" {
  name            = "${var.name_prefix}-service"
  cluster         = aws_ecs_cluster.this.id
  task_definition = aws_ecs_task_definition.this.arn
  desired_count   = var.desired_count
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = var.private_subnet_ids
    security_groups  = [aws_security_group.service.id]
    assign_public_ip = false
  }

  load_balancer {
    target_group_arn = var.target_group_arn
    container_name   = "franchise-api"
    container_port   = var.container_port
  }

  lifecycle {
    ignore_changes = [desired_count] # Managed by auto scaling
  }
}

# MiniStack does not implement Application Auto Scaling, so this is disabled
# there (enable_autoscaling = false). It stays for real AWS deployments.
resource "aws_appautoscaling_target" "this" {
  count              = var.enable_autoscaling ? 1 : 0
  service_namespace  = "ecs"
  resource_id        = "service/${aws_ecs_cluster.this.name}/${aws_ecs_service.this.name}"
  scalable_dimension = "ecs:service:DesiredCount"
  min_capacity       = var.min_capacity
  max_capacity       = var.max_capacity
}

resource "aws_appautoscaling_policy" "cpu" {
  count              = var.enable_autoscaling ? 1 : 0
  name               = "${var.name_prefix}-cpu-scaling"
  policy_type        = "TargetTrackingScaling"
  service_namespace  = aws_appautoscaling_target.this[0].service_namespace
  resource_id        = aws_appautoscaling_target.this[0].resource_id
  scalable_dimension = aws_appautoscaling_target.this[0].scalable_dimension

  target_tracking_scaling_policy_configuration {
    target_value       = 60
    scale_in_cooldown  = 120
    scale_out_cooldown = 60

    predefined_metric_specification {
      predefined_metric_type = "ECSServiceAverageCPUUtilization"
    }
  }
}
