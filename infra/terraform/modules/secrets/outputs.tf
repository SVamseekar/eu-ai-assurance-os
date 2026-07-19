output "secrets_prefix" {
  description = "Placeholder secret manager prefix / path."
  value       = local.secrets_prefix
}

output "secret_names" {
  description = "Logical secret names operators must provision."
  value       = var.secret_names
}
