variable "name_prefix" {
  type = string
}

variable "secret_names" {
  description = "Logical secret names the API expects (values live outside TF)."
  type        = list(string)
}

variable "tags" {
  type    = map(string)
  default = {}
}
