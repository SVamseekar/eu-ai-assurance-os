terraform {
  required_version = ">= 1.5.0"

  required_providers {
    # Skeleton uses the null provider only so `terraform init/validate` works
    # without cloud credentials. Replace modules with real AWS/GCP providers
    # before any production apply.
    null = {
      source  = "hashicorp/null"
      version = "~> 3.2"
    }
  }

  # Default local backend — no remote state until you configure one.
  # backend "s3" { ... } or "gcs" { ... } belongs in a private override, not git.
}
