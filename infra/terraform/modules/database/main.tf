# Database skeleton — replace with RDS Postgres or Cloud SQL + pgvector notes.
resource "null_resource" "database_placeholder" {
  triggers = {
    name_prefix    = var.name_prefix
    instance_class = var.instance_class
    network_id     = var.network_id
  }
}

locals {
  endpoint = "jdbc:postgresql://${var.name_prefix}-db.${var.region}.example:5432/eu_ai_assurance"
}
