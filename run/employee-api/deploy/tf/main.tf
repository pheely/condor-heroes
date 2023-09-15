locals {
  services = [
    "run.googleapis.com",
    "clouddeploy.googleapis.com",
    "artifactregistry.googleapis.com",
  ]
}

resource "google_project_service" "enabled_service" {
  for_each = toset(local.services)
  project  = var.project_id
  service  = each.key

  provisioner "local-exec" {
    command = "sleep 15"
  }

  provisioner "local-exec" {
    when    = destroy
    command = "sleep 5"
  }
}

resource "google_clouddeploy_target" "ist" {
    location = var.region_id
    name = "ist"

    description = "Employee API IST"

    require_approval = false

    run {
        location = "projects/${var.project_id}/locations/${var.region_id}"
    }
}

resource "google_clouddeploy_target" "prd" {
    location = var.region_id
    name = "prd"

    description = "Employee API PRD"

    require_approval = true

    run {
        location = "projects/${var.project_id}/locations/${var.region_id}"
    }
}

resource "google_clouddeploy_target" "uat" {
    location = var.region_id
    name = "uat"

    description = "Employee API UAT"

    require_approval = false

    run {
        location = "projects/${var.project_id}/locations/${var.region_id}"
    }
}

resource "google_clouddeploy_delivery_pipeline" "primary" {
    location = var.region_id
    name = "employee-api-cd-pipeline"

    description = "Cloud Deploy delivery pipeline for Employee API"

    project = var.project_id

    serial_pipeline {
        stages {
            profiles = [google_clouddeploy_target.ist.name]
            target_id = "ist"
        }
        stages {
            profiles = [google_clouddeploy_target.uat.name]
            target_id = "uat"
        }
        stages {
            profiles = [google_clouddeploy_target.prd.name]
            target_id = "prd"
        }
    }
}
