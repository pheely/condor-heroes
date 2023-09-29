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

variable "sa_name" {
  description = "service account for pushing pub/sub message to cloud run"
  type        = string
}

variable "sa_display_name" {
  description = "display name for service account to push pub/sub msg to cloud run"
  type        = string
}

variable "roles" {
  description = "Name of the IAM roles to be assigned for the service account"
  type        = list(string)
}

variable "topic_name" {
  description = "Pub/sub topic name"
  type        = string
}
