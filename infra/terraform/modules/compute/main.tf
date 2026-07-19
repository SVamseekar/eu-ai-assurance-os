# Compute skeleton — replace with Cloud Run, ECS Fargate, or GCE MIG.
# Wire container image from infra/Dockerfile and inject secrets from module.secrets.

resource "null_resource" "compute_placeholder" {
  triggers = {
    name_prefix = var.name_prefix
    cpu         = var.cpu
    memory      = var.memory
    network_id  = var.network_id
    db          = var.database_endpoint
  }
}

locals {
  service_url   = "https://api-${var.name_prefix}.example.com"
  object_bucket = var.enable_object_storage ? "${var.name_prefix}-evidence" : ""
}
