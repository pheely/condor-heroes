gcloud deploy releases create employee-api-release-001 \
  --project=ibcwe-event-layer-f3ccf6d9 \
  --region=us-central1 \
  --delivery-pipeline=delivery-pipeline-for-employee-api \
  --images=employee-api-image=us-central1-docker.pkg.dev/ibcwe-event-layer-f3ccf6d9/cloud-run-try/employee@sha256:2cb0c20cec6fac7b83ab4c75fde2a01e8e4b2e4220a598c7976d7963d32644df
