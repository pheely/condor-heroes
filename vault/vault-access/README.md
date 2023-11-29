# Accessing Hashicorp Vault

This shows how to save a version 2 KV secret into the Vault and retrieve it 
back.

## Prerequisites

The vault is started and unsealed.

Use the following command to enable v2 `kv` secret engine.
```bash
vault secrets enable -path secret kv-v2
```