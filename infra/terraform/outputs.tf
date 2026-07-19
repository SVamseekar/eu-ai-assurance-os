output "network_id" {
  description = "Placeholder network identifier from the network module."
  value       = module.network.network_id
}

output "database_endpoint" {
  description = "Placeholder JDBC-style endpoint for the managed database."
  value       = module.database.endpoint
}

output "secrets_prefix" {
  description = "Secret manager path/prefix for API secrets."
  value       = module.secrets.secrets_prefix
}

output "api_service_url" {
  description = "Placeholder public URL for the API compute service."
  value       = module.compute.service_url
}

output "object_bucket" {
  description = "Evidence object storage bucket name placeholder."
  value       = module.compute.object_bucket
}
