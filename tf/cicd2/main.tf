resource "google_project_service" "enabled_service" {
  project = var.project_id
  service = "sourcerepo.googleapis.com"
}

resource "google_sourcerepo_repository" "repo" {
  #  depends_on = [google_project_service.enabled_service]
  name    = "${var.namespace}-repo"
  project = var.project_id
}

