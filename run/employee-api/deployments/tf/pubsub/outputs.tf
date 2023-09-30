output sa_email {
  value = google_service_account.this.email
}

output topic {
  value = google_pubsub_topic.this.name
}
