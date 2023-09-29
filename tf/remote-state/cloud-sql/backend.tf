terraform {
  backend "gcs" {
    bucket = "ibcwe-event-layer-f3ccf6d9-tf-state"
    prefix = "employees/cloud-sql"
  }
}
