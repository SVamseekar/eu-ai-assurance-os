variable "name_prefix" {
  type = string
}

variable "region" {
  type = string
}

variable "cpu" {
  type = string
}

variable "memory" {
  type = string
}

variable "network_id" {
  type = string
}

variable "database_endpoint" {
  type = string
}

variable "secrets_prefix" {
  type = string
}

variable "enable_object_storage" {
  type = bool
}

variable "tags" {
  type    = map(string)
  default = {}
}
