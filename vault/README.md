# Accessing Hashicorp Vault

## Starting and Configuring Vault

<details><summary style="color:Maroon;font-size:16px;">Show Contents</summary>

### Setup
<details><summary style="color:Maroon;font-size:16px;">Show Contents</summary>

Run the following commands to set up the environment for Vault server
```bash
mkdir -p ~/try/vault
cd ~/try/vault

tee config.hcl <<EOF
storage "raft" {
  path    = "./vault/data"
  node_id = "node1"
}

listener "tcp" {
  address     = "127.0.0.1:8200"
  tls_disable = "true"
}

api_addr = "http://127.0.0.1:8200"
cluster_addr = "https://127.0.0.1:8201"
ui = true
EOF

mkdir -p vault/data
```

</details>

### Starting the Server
<details><summary style="color:Maroon;font-size:16px;">Show Contents</summary>

Run the following commands to start the server.
```bash
cd ~/try/vault
vault server -config=config.hcl
```

</details>

### Exposing the Endpoint (Optional)

<details><summary style="color:Maroon;font-size:16px;">Show Contents</summary>

Run the following command to start ngrok and connect to Vault:
```bash
ngrok http http://127.0.0.1:8200
```

Copy the **Forwarding** address including `https://`.

---

**Note**: to install ngrok, run `sudo snap install ngrok`.

---

</details>

### Shutting down the Server
<details><summary style="color:Maroon;font-size:16px;">Show Contents</summary>

Run the following commands to shut down the Vault server.
```bash
pgrep -f vault | xargs kill
```

**Note**: the vault will be sealed during the shutdown phase. We need to unseal it after the next start.

</details>

### Initializing the Server
<details><summary style="color:Maroon;font-size:16px;">Show Contents</summary>

To initialize the vault, run the following commands on a separate terminal:
```bash
export VAULT_ADDR='http://127.0.0.1:8200'
vault operator init
```

Store the five keys and root token in a safe place. 

</details>

### Unsealing the Vault

<details><summary style="color:Maroon;font-size:16px;">Show Contents</summary>

You need to unseal the vault after the server is initialized the first time and everytime the server is restarted.

To unseal the vault, ensure the environment variable is set:
```bash
export VAULT_ADDR='http://127.0.0.1:8200'
```

And run the following commands three times with different keys:
```bash
vault operator unseal
```

</details>

### Authenticate to Vault as `root`
<details><summary style="color:Maroon;font-size:16px;">Show Contents</summary>

Run the following command when asked provide the root token.
```bash
export VAULT_ADDR=http://127.0.0.1:8200
vault login
```
Or alternatively, set the environment variables:
```bash
export VAULT_TOKEN=hvs.yejqgGeFsOpUwPAQbDRvSbdO
export VAULT_ADDR=http://127.0.0.1:8200
```

</details>

### Enable the v2 `kv` Secret Engine
<details><summary style="color:Maroon;font-size:16px;">Show Contents</summary>

```bash
vault secrets enable -path secret kv-v2
vault kv put -mount=secret top-secret password=good4Now!
```

</details>

### Enable the v1 `kv` Secret Engine
<details><summary style="color:Maroon;font-size:16px;">Show Contents</summary>

```bash
vault secrets enable -path secret-v1 kv
```

</details>

### Setting up GCP Auth Method
<details><summary style="color:Maroon;font-size:16px;">Show Contents</summary>

Reference: https://developer.hashicorp.com/vault/tutorials/auth-methods/gcp-auth-method

#### GCP Side Setup

A service account with the following permissions, and its JSON key file are required:
- iam.serviceAccounts.get (included in roles/iam.serviceAccountUser)
- iam.serviceAccountKeys.get (included in roles/iam.serviceAccountViewer)
- compute.instances.get (included roles/compute.viewer)
- compute.instanceGroups.list (included roles/compute.viewer)
- iam.serviceAccounts.signJwt (included in roles/iam.serviceAccountTokenCreator)

**Service Account**: gyre-dataflow-ist@ibcwe-event-layer-f3ccf6d9.iam.gserviceaccount.com. 

**Key File**: `VaultServiceAccountKey.json`.

#### Vault Setup

1. Authenticate to the Vault as the root
2. Set environment variable for the GCP service account
    ```bash
    export GCP_SERVICE_EMAIL=gyre-dataflow-ist@ibcwe-event-layer-f3ccf6d9.iam.gserviceaccount.com
    export GCP_PROJECT=ibcwe-event-layer-f3ccf6d9
    ```
3. Enable the GCP secrets engine
    ```bash
    vault auth enable gcp
    ```
4. Configure the GCP auth method to use the `VaultServiceAccountKey.json` credentials.
    ```bash
    vault write auth/gcp/config \
    credentials=@VaultServiceAccountKey.json
    ```
5. Create a policy file `policy.hcl`.
    ```bash
    tee policy.hcl <<EOF
    # Read permission on the k/v secrets
    path "/secret/*" {
        capabilities = ["read", "list"]
    }
    EOF
    ```
6. vault policy write dev policy.hcl
    ```bash
    vault policy write gcp policy.hcl
    ```
7. Create a role for IAM service account.
    ```bash
    vault write auth/gcp/role/vault-iam-auth-role \
    type="iam" \
    policies="gcp" \
    bound_service_accounts="$GCP_SERVICE_EMAIL"
    ```
8. Create a role for GCE instances.
    ```bash
    vault write auth/gcp/role/vault-gce-auth-role \
    type="gce" \
    policies="gcp" \
    bound_projects=$GCP_PROJECT \
    bound_zones="us-east1-b"
    ```

</details>

</details>

## Authentication to Vault using GCP Cloud IAM

<details><summary style="color:Maroon;font-size:16px;">Show Contents</summary>

```bash
vault login -method=gcp \
role="vault-iam-auth-role" \
service_account="$GCP_SERVICE_EMAIL" \
jwt_exp="15m" \
credentials=@VaultServiceAccountKey.json
```

Run the following command to retrieve a secret:
```bash
vault kv get -mount secret top-secret
```

</details>

## Authentication to Vault from a GCE

<details><summary style="color:Maroon;font-size:16px;">Show Contents</summary>

1. Create a GCE instance in the `us-east1-b` zone:
    ```bash
    gcloud compute instances create vault-auth-test --zone us-east1-b \
    --service-account gyre-dataflow-ist@ibcwe-event-layer-f3ccf6d9.iam.gserviceaccount.com
    ```
2. SSH to the VM:
    ```bash
    gcloud compute ssh vault-auth-test --zone=us-east1-b 
    ```
3. Install Vault
    ```bash
    curl -fsSL https://apt.releases.hashicorp.com/gpg | sudo gpg --dearmor -o /usr/share/keyrings/hashicorp-archive-keyring.gpg
    echo "deb [signed-by=/usr/share/keyrings/hashicorp-archive-keyring.gpg] https://apt.releases.hashicorp.com $(lsb_release -cs) main" | sudo tee /etc/apt/sources.list.d/hashicorp.list
    sudo apt update && sudo apt install vault
    ```
6. Install JDK
    ```bash
    sudo apt install -y wget apt-transport-https
    sudo mkdir -p /etc/apt/keyrings
    sudo wget -O - https://packages.adoptium.net/artifactory/api/gpg/key/public | sudo tee /etc/apt/keyrings/adoptium.asc
    sudo echo "deb [signed-by=/etc/apt/keyrings/adoptium.asc] https://packages.adoptium.net/artifactory/deb $(awk -F= '/^VERSION_CODENAME/{print$2}' /etc/os-release) main" | sudo tee /etc/apt/sources.list.d/adoptium.list
    sudo apt update 
    sudo apt install temurin-17-jdk
    ```
7. Set an environment variable for the Vault ngrok address.
    ```bash
    export VAULT_ADDR=<actual-address-from-ngrok>
    ```
8. Authenticate with Vault using the `vault-gce-auth-role role`.
    ```bash
    vault login -method=gcp role="vault-gce-auth-role"
    ```
9. Retrieve a secret
    ```bash
    vault kv get -mount secret top-secret
    ``` 
10. Connect and retrieve secrets in Java app
    ```bash
    gsutil cp gs://philip-innovate-staging/vault-access.jar .
    java -jar vault-access.jar
    ```
11. Disconnect the testing VM
    ```bash
    exit
    ```
12. Delete the testing VM
    ```bash
    gcloud compute instances delete vault-auth-testing --zone us-east1-b
    ```
</details>

