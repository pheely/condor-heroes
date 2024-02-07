#!/usr/bin/bash

## GCP project
EDITOR_NAME=philip.yang@scotiabank.com
PROJECT_ID=ibcwe-event-layer-f3ccf6d9
REGION=us-central1

## Artifact Registry
REPOSITORY=us-central1-docker.pkg.dev/$PROJECT_ID/cloud-run-try

## Database
INSTANCE_NAME=sql-db
DB_VERSION=MYSQL_8_0
DB_NAME=hr
DB_USER=user
PASSWORD=changeit

## Redis memorystore
REDIS_INSTANCE=employee
REDIS_TIER=basic
REDIS_SIZE=1

## VPC connector
VPC_CONNECTOR=employee
VPC_IP_RANGE="10.0.0.0/28"

## Secret manager secret
SECRET_NAME=DB_PASS

## Pubsub 
TOPIC_NAME=employee_creation
SUBSCRIPTION_NAME=employee-creation-sub


## Cloud run
SERVICE_NAME=employee-service
CLOUD_RUN_SA="gyre-dataflow@${PROJECT_ID}.iam.gserviceaccount.com"
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

# Create a Cloud SQL instance named sql-db
gcloud sql instances create $INSTANCE_NAME \
--tier db-f1-micro \
--database-version $DB_VERSION \
--region $REGION \
--root-password $PASSWORD
 
# Create the hr database
gcloud sql databases create $DB_NAME --instance $INSTANCE_NAME
 
# Create a db user
gcloud sql users create $DB_USER --instance $INSTANCE_NAME --password $PASSWORD
 
# Create a redis instance named employee
gcloud redis instances create $REDIS_INSTANCE --tier $REDIS_TIER --size $REDIS_SIZE --region $REGION
 
# Obtain and copy the redis IP and port that are required later
REDIS_PORT=$(gcloud redis instances list --region $REGION --format="value(port)")
REDIS_IP=$(gcloud redis instances list --region $REGION --format="value(RESERVED_IP)")
 
# Create a VPC access connector named employee
gcloud compute networks vpc-access connectors create $VPC_CONNECTOR \
--region $REGION \
--range $VPC_IP_RANGE

# Create a secret in Secret Manager to hold the database user password
echo -n "$PASSWORD" | gcloud secrets create $SECRET_NAME --replication-policy automatic --data-file=-

# get the latest enabled version of secret
SECRET_VERSION=$(gcloud secrets versions list $SECRET_NAME --filter="STATE=enabled" --format="value(NAME)" --sort-by ~CREATED --limit 1)


# Create a Pubsub topic named employee_creation
gcloud pubsub topics create $TOPIC_NAME

# Create a service account to be used provide the OIDC token when push pubsub message to cloud run
gcloud iam service-accounts create $INVOKER_SA --display-name "SA calling employee api"

# Obtain the service account's email address that is required later
INVOKER_EMAIL=$(gcloud iam service-accounts list --format='value(email)' --filter="name~$INVOKER_SA")

# Enable project's Pub/Sub service agent to create access tokens
PROJECT_NUMBER=$(gcloud projects list \
--filter="ibcwe-event-layer" \
--format='value(PROJECT_NUMBER)')

gcloud projects add-iam-policy-binding $PROJECT_ID \
--member=serviceAccount:service-$PROJECT_NUMBER@gcp-sa-pubsub.iam.gserviceaccount.com \
--role=roles/iam.serviceAccountTokenCreator

# Create the cloud Run service
gcloud run deploy $SERVICE_NAME --image $REPOSITORY/$SERVICE_NAME \
--service-account=$CLOUD_RUN_SA \
--add-cloudsql-instances "${PROJECT_ID}:${REGION}:${INSTANCE_NAME}" \
--vpc-connector $VPC_CONNECTOR \
--no-allow-unauthenticated \
--set-env-vars DB_USER=$DB_USER \
--set-secrets DB_PASS="${SECRET_NAME}:${SECRET_VERSION}" \
--set-env-vars DB_NAME=$DB_NAME \
--set-env-vars DB_PRIVATE_IP= \
--set-env-vars INSTANCE_CONNECTION_NAME="${PROJECT_ID}:${REGION}:${INSTANCE_NAME}" \
--set-env-vars REDIS_HOST=${REDIS_IP%/*} \
--set-env-vars REDIS_PORT=$REDIS_PORT

# Obtain the URI of the Employee API service and set it as an environment var
EMPLOYEE_API=$(gcloud run services describe $SERVICE_NAME --format='value(status.address.url)')

# IAM policy binding: grant the service account the "role/run.invoker" role
# to the Employee API Cloud Run service.

gcloud run services add-iam-policy-binding $SERVICE_NAME \
--member=serviceAccount:"${INVOKER_EMAIL}" \
--role=roles/run.invoker

# Create a Pubsub subscription to push message to the Cloud Run service
gcloud pubsub subscriptions create $SUBSCRIPTION_NAME \
--topic $TOPIC_NAME \
--push-endpoint=$EMPLOYEE_API \
--push-auth-service-account="${INVOKER_EMAIL}"

# Get dbpassword from secret manager
MYSQL_PASS=$(gcloud secrets versions access $SECRET_VERSION --secret=$SECRET_NAME)

# Create and poplate tables
cloud-sql-proxy --port 3306 ${PROJECT_ID}:${REGION}:${INSTANCE_NAME} &
sleep 15
mysql -u root -p$MYSQL_PASS --host 127.0.0.1 < ../../schema.sql

# stop sqlproxy
pgrep -f cloud-sql-proxy|xargs kill

