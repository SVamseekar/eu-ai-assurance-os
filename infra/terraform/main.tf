# Root module — composes skeleton child modules.
# This configuration is intentionally non-provisioning: modules emit null
# resources and documentation outputs only. Do NOT apply against a real
# account until modules are replaced with provider-backed resources.

locals {
  name_prefix = "${var.project_name}-${var.environment}"
}

module "network" {
  source = "./modules/network"

  name_prefix = local.name_prefix
  region      = var.region
  tags        = var.tags
}

module "database" {
  source = "./modules/database"

  name_prefix    = local.name_prefix
  region         = var.region
  instance_class = var.db_instance_class
  network_id     = module.network.network_id
  tags           = var.tags
}

module "secrets" {
  source = "./modules/secrets"

  name_prefix = local.name_prefix
  tags        = var.tags

  # Names only — values live in a real secret manager, never in tfvars.
  secret_names = [
    "EVAL_CALLBACK_SECRET",
    "AUDIT_CHAIN_SECRET",
    "DATABASE_PASSWORD",
    "OAUTH_GOOGLE_CLIENT_SECRET",
    "OAUTH_MICROSOFT_CLIENT_SECRET",
  ]
}

module "compute" {
  source = "./modules/compute"

  name_prefix           = local.name_prefix
  region                = var.region
  cpu                   = var.api_cpu
  memory                = var.api_memory
  network_id            = module.network.network_id
  database_endpoint     = module.database.endpoint
  secrets_prefix        = module.secrets.secrets_prefix
  enable_object_storage = var.enable_object_storage
  tags                  = var.tags
}
