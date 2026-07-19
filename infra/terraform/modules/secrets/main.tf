# Secrets skeleton — replace with AWS Secrets Manager / GCP Secret Manager refs.
# Values must never be stored in Terraform state from this repo's examples.

resource "null_resource" "secrets_placeholder" {
  triggers = {
    name_prefix  = var.name_prefix
    secret_names = join(",", sort(var.secret_names))
  }
}

locals {
  secrets_prefix = "sm://${var.name_prefix}"
}
