# Accessing Hashicorp Vault

This shows how to save a version 2 KV secret into the Vault and retrieve it 
back.

## Setting up and Initializing the Server

<details><summary style="color:Maroon;font-size:16px;">Show Contents</summary>

Run the following commands to start the server.
```bash
mkdir -p ~/try/vault
cd ~/try/vault

cat > config.hcl <<EOF
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

vault server -config=config.hcl
```

To initialize the vault, run the following commands on a separate terminal:
```bash
export VAULT_ADDR='http://127.0.0.1:8200'
vault operator init
```

Store the five keys and root token in a safe place. 

</details>

## Unsealing the Vault

<details><summary style="color:Maroon;font-size:16px;">Show Contents</summary>

To unseal the vault, run the following commands three times with different keys:
```bash
vault operator unseal
```

</details>

## Authenticate to Vault as `admin` and Enable v2 `kv` Secret Engine
<details><summary style="color:Maroon;font-size:16px;">Show Contents</summary>

Run the following command when asked provide the root token.
```bash
vault login
```
Or alternatively, set the environment variables:
```bash
export VAULT_TOKEN=hvs.yejqgGeFsOpUwPAQbDRvSbdO
export VAULT_ADDR=http://127.0.0.1:8200
```

Use the following command to enable v2 `kv` secret engine.
```bash
vault secrets enable -path secret kv-v2
vault kv put -mount=secret top-secret password=good4Now!
```

</details>

## GCP Auth Method
<details><summary style="color:Maroon;font-size:16px;">Show Contents</summary>

Reference: https://developer.hashicorp.com/vault/tutorials/auth-methods/gcp-auth-method

### GCP Side Setup

<details><summary style="color:Maroon;font-size:16px;">Show Contents</summary>

#### Service Account and Json Key File

A service account with the following permissions, and its JSON key file are required:
- iam.serviceAccounts.get (included in roles/iam.serviceAccountUser)
- iam.serviceAccountKeys.get (included in roles/iam.serviceAccountViewer)
- compute.instances.get (included roles/compute.viewer)
- compute.instanceGroups.list (included roles/compute.viewer)
- iam.serviceAccounts.signJwt (included in roles/iam.serviceAccountTokenCreator)

**Service Account**: gyre-dataflow-ist@ibcwe-event-layer-f3ccf6d9.iam.gserviceaccount.com. 

**Key File**: `VaultServiceAccountKey.json`.

</details>

### Vault Setup
<details><summary style="color:Maroon;font-size:16px;">Show Contents</summary>

1. Authenticate to the Vault
2. Set environment variable for the GCP service account
    ```bash
    export GCP_SERVICE_EMAIL=gyre-dataflow-ist@ibcwe-event-layer-f3ccf6d9.iam.gserviceaccount.com
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
</details>


### Authentication to Vault using GCP Cloud IAM

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
vault kv -mount secret get top-secret
```

</details>

</details>