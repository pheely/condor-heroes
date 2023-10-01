#!/bin/sh

if [ $# -ne 1 ]; then
  echo "$0 [UC1 | UE4]"
  exit 1
fi

if [ $1 = "UC1" ]; then
  REGION="us-central1"
else
  REGION="us-east4"
fi

echo "REGION: $REGION"

gcloud deploy releases create employee-api-release-002 \
  --project=ibcwe-event-layer-f3ccf6d9 \
  --region=$REGION \
  --delivery-pipeline=employee-api-cd-pipeline \
  --images=employee-api-image=us-central1-docker.pkg.dev/ibcwe-event-layer-f3ccf6d9/cloud-run-try/employee@sha256:2cb0c20cec6fac7b83ab4c75fde2a01e8e4b2e4220a598c7976d7963d32644df
