variable "name_prefix" {
  type = string
}

variable "region" {
  type = string
}

variable "instance_class" {
  type = string
}

variable "network_id" {
  type = string
}

variable "tags" {
  type    = map(string)
  default = {}
}
