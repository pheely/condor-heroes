#!/usr/bin/bash

## GCP project
EDITOR_NAME=philip.yang@scotiabank.com
PROJECT_ID=ibcwe-event-layer-f3ccf6d9
REGION=us-central1

## Database
INSTANCE_NAME=sql-db
DB_NAME=hr
DB_USER=user

## Secret manager secret
SECRET_NAME=employee-db-user-pw

gcloud config set project $PROJECT_ID
gcloud config set account $EDITOR_NAME
gcloud config set compute/region $REGION
gcloud config set compute/zone $REGION-a
gcloud config set run/region $REGION

# Enable APIs & Services
gcloud services enable secretmanager.googleapis.com
gcloud services enable sqladmin.googleapis.com
gcloud services enable sql-component.googleapis.com

# get the latest enabled version of secret
SECRET_VERSION=$(gcloud secrets versions list $SECRET_NAME --filter="STATE=enabled" --format="value(NAME)" --sort-by ~CREATED --limit 1)


# Get dbpassword from secret manager
MYSQL_PASS=$(gcloud secrets versions access $SECRET_VERSION --secret=$SECRET_NAME)

# Create and poplate tables
cloud-sql-proxy --port 3306 ${PROJECT_ID}:${REGION}:${INSTANCE_NAME} &
sleep 15
mysql -u user -p$MYSQL_PASS --host 127.0.0.1 < ../../../schema.sql

# stop sqlproxy
pgrep -f cloud-sql-proxy|xargs kill

