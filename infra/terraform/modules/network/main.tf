# Network skeleton — replace with VPC / VPC connectors / private subnets.
resource "null_resource" "network_placeholder" {
  triggers = {
    name_prefix = var.name_prefix
    region      = var.region
  }
}

locals {
  # Stable fake id so root outputs stay non-empty after plan/apply of skeleton.
  network_id = "net-${var.name_prefix}-${var.region}"
}
