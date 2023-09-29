variable "project_id" {
  type        = string
  default     = "ibcwe-event-layer-f3ccf6d9"
  description = "The Google Cloud Project ID to use"
}

variable "region" {
  type        = string
  default     = "us-central1"
  description = "The Google Cloud compute region to use"
}

variable "services" {
  description = "List of Services to be enabled"
  type        = list(string)
}

variable "vpc_con" {
  description = "name of the vpc access connector"
  type        = string
}

variable "vpc_con_cidr" {
  description = "ip range of the vpc access connector"
  type        = string
}

variable "redis_name" {
  description = "name of redis instance"
  type        = string
}

variable "redis_tier" {
  description = "redis tier"
  type        = string
}

variable "redis_size" {
  description = "redis size"
  type        = number
}

variable "db_settings" {
  description = "Map of the various DB Settings"
  type        = map(any)
}

variable "secret_root_pw" {
  description = "secret id for CloudSQL root password"
  type        = string
}

variable "secret_user_pw" {
  description = "secret id for CloudSQL user password"
  type        = string
}
