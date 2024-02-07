#!/usr/bin/bash

## GCP project
EDITOR_NAME=philip.yang@scotiabank.com
PROJECT_ID=ibcwe-event-layer-f3ccf6d9
REGION=us-central1

## Artifact Registry
REPOSITORY=cloud-run-try

## Database
INSTANCE_NAME=sql-db

## Redis memorystore
REDIS_INSTANCE=employee

## VPC connector
VPC_CONNECTOR=employee

## Secret manager secret
SECRET_NAME=DB_PASS

## Pubsub 
TOPIC_NAME=employee_creation
SUBSCRIPTION_NAME=employee-creation-sub


## Cloud run
SERVICE_NAME=employee-service
INVOKER_SA=employee-api-invoker

gcloud config set project $PROJECT_ID
gcloud config set account $EDITOR_NAME
gcloud config set compute/region $REGION
gcloud config set compute/zone $REGION-a
gcloud config set run/region $REGION

# Enable APIs & Services
gcloud services enable run.googleapis.com
gcloud services enable secretmanager.googleapis.com
gcloud services enable artifactregistry.googleapis.com
gcloud services enable sqladmin.googleapis.com
gcloud services enable sql-component.googleapis.com
gcloud services enable pubsub.googleapis.com
gcloud services enable redis.googleapis.com
gcloud services enable vpcaccess.googleapis.com


gcloud run services delete $SERVICE_NAME
gcloud sql instances delete $INSTANCE_NAME
gcloud iam service-accounts delete "${INVOKER}@${PROJECT_ID}.iam.gserviceaccount.com"
gcloud pubsub subscriptions delete $SUBSCRIPTION_NAME
gcloud pubsub topics delete $TOPIC_NAME
gcloud redis instances delete $REDIS_INSTANCE --region $REGION
gcloud compute networks vpc-access connectors delete $VPC_CONNECTOR --region $REGION
gcloud secrets delete $SECRET_NAME
gcloud artifacts packages delete $SERVICE_NAME --repository=$REPOSITORY --location=$REGION

