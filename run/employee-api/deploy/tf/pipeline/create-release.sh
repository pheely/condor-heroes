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

gcloud deploy releases create employee-api-release-001 \
  --project=ibcwe-event-layer-f3ccf6d9 \
  --region=$REGION \
  --delivery-pipeline=employee-api-cd-pipeline \
  --images=employee-api-image=us-central1-docker.pkg.dev/ibcwe-event-layer-f3ccf6d9/cloud-run-try/employee@sha256:25480ea2d9e912e8571580dd9cd95923b480ee265c1a28dbff670754b68fac45
