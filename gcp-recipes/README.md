# GCP Recipes

## Google Cloud SDK

### Authentication

#### Using a service account with a account key

```bash
gcloud config set project ibcwe-event-layer-f3ccf6d9
gcloud auth activate-service-account --key-file=/home/ext_philip_yang_scotiabank_com/keys/gyre-dataflow-ist1.json
gcloud auth list
```

### KMS

#### How to create a KMS keyring

```bash
gcloud kms keyrings create "fpe-poc-keyring" --location global
```

#### How to create a symmetric crypto key

```bash
gcloud kms keys create "fpe-poc-key" --location global --keyring fpe-poc-keyring --purpose encryption
```

Verify the key is created successfully
```bash
gcloud kms keys list --location global --keyring fpe-poc-keyring
```

#### How to encrypt a secret using a KMS key

```bash
# create a AES key and base64 encode it
openssl rand -out aes_key.bin 32
base64 -i aes_key.bin > aes_key.enc

gcloud kms encrypt --ciphertext-file=aes_key.cipher --plaintext-file=aes_key.enc --key=fpe-poc-key --keyring=fpe-poc-keyring --location=global 
```

### Cloud Storage

#### Create a storage bucket

```bash
gcloud storage buckets create gs://philip-test-bucket-cmek
```

#### Enable CMEK and versioning on existing bucket

```bash
gcloud storage buckets update gs://philip-test-bucket-cmek \
--versioning \
--default-encryption-key=projects/ibcwe-event-layer-f3ccf6d9/locations/us/keyRings/bucket-cmek-keyring/cryptoKeys/bucket-cmek
```