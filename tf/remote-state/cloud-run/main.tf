resource "google_cloud_run_v2_service" "this" {
  name     = "employee-api"
  location = var.region

  template {
    volumes {
      name = "cloudsql"
      cloud_sql_instance {
        instances = [data.terraform_remote_state.cloud_sql.outputs.connection_name]
      }
    }

    containers {
      image = var.image

      env {
        name = "DB"
        value = "mysql://employee-api:changeit@unix(/cloudsql/ibcwe-event-layer-f3ccf6d9:us-central1:sql-db)/hr"
      }
      volume_mounts {
        name = "cloudsql"
        mount_path = "/cloudsql"
      }
    }
  }
}

