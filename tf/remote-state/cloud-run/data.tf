data "terraform_remote_state" "cloud_sql" {
  backend = "gcs"
  config = {
    bucket = "ibcwe-event-layer-f3ccf6d9-tf-state"
    prefix = "employees/cloud-sql"
  }
}

output "connection_name" {
  value = data.terraform_remote_state.cloud_sql.outputs.connection_name
}
