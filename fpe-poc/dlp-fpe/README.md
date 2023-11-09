# Format Preserving Encryption using DLP API

## Service Account

I will be using  gyre-dataflow-ist@ibcwe-event-layer-f3ccf6d9.iam.gserviceaccount.com.

It has the following roles:
- roles/cloudkms.admin: required to create kingrings and cryptoKeys
- roles/cloudkms.cryptoKeyEncrypterDecrypter: required to use the cryptoKey
- roles/dlp.admin: required to call DLP API

## Create a wrapped key

1. Use the appropriate service account
	```bash
	gcloud config set project ibcwe-event-layer-f3ccf6d9
	gcloud auth activate-service-account --key-file=/home/ext_philip_yang_scotiabank_com/keys/gyre-dataflow-ist1.json
	gcloud auth list
	```
2. Create a keyring
	```bash
	gcloud kms keyrings create "fpe-poc-keyring" --location global
	```
3. Create a symmetric encryption key
	```bash
	gcloud kms keys create "fpe-poc-key" --location global --keyring fpe-poc-keyring --purpose encryption
	```
4. Verify the key is created successfully
	```bash
	gcloud kms keys list --location global --keyring fpe-poc-keyring
	```
5. create a AES key and base64 encode it
	```bash
	openssl rand -out aes_key.bin 32
	base64 -i aes_key.bin
	```
6. Wrap the AES key
	```bash
	curl https://cloudkms.googleapis.com/v1/projects/ibcwe-event-layer-f3ccf6d9/locations/global/keyRings/fpe-poc-keyring/cryptoKeys/fpe-poc-key:encrypt \
	-X POST \
	-H "Authorization: Bearer $(gcloud auth print-access-token)" \
	-H "content-type: application/json" \
	--data "{\"plaintext\": \"mXC+RrazJKJTaEUQbJBZwQDu3ARhaB+FFmYxgV5aqS0=\"}"
	```
	The output is similar to this:
	```text
	{
	  "name": "projects/ibcwe-event-layer-f3ccf6d9/locations/global/keyRings/fpe-poc-keyring/cryptoKeys/fpe-poc-key/cryptoKeyVersions/1",
	  "ciphertext": "CiQA+yi9TI2anWIJ9WGrZe4zeee9FQgFunT1YXafzLcWGgWQZj8SSQCfIIegX+iV9gc7NArQqZAM7GJ5CQMJjTv3r7PyvbgRfORvw2vNOi2pdcWg6BlOih7u8qfleQQR+3ulFad1+fpz99sS/vXIcgc=",
	  "ciphertextCrc32c": "4281793573",
	  "protectionLevel": "SOFTWARE"
	}
	```

## Run the app calling DLP API

Provide the service account key file.
```bash
export GOOGLE_APPLICATION_CREDENTIALS=~/keys/gyre-dataflow-ist1.json
```
