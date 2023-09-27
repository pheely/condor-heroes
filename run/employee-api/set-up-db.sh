#!/bin/bash
cloud-sql-proxy --port 3306 ibcwe-event-layer-f3ccf6d9:us-central1:sql-db &
sleep 15
mysql -u root -p --host 127.0.0.1 < schema.sql

