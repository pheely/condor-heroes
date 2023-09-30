data "terraform_remote_state" "foundation" {
  backend = "gcs"
  config = {
    bucket = "ibcwe-event-layer-f3ccf6d9-tf-state"
    prefix = "run-poc/employee/foundation"
  }
}

data "terraform_remote_state" "pubsub" {
  backend = "gcs"
  config = {
    bucket = "ibcwe-event-layer-f3ccf6d9-tf-state"
    prefix = "run-poc/employee/pubsub"
  }
}
