# HashiCorp Vault

## Installation

```bash
# Download the signing key to a new keyring
wget -O- https://apt.releases.hashicorp.com/gpg | sudo gpg --dearmor -o /usr/share/keyrings/hashicorp-archive-keyring.gpg

# Verify the key's fingerprint
# The fingerprint must match 798A EC65 4E5C 1542 8C8E 42EE AA16 FCBC A621 E701
gpg --no-default-keyring --keyring /usr/share/keyrings/hashicorp-archive-keyring.gpg --fingerprint

# Add the HashiCorp repo
echo "deb [signed-by=/usr/share/keyrings/hashicorp-archive-keyring.gpg] https://apt.releases.hashicorp.com $(lsb_release -cs) main" | sudo tee /etc/apt/sources.list.d/hashicorp.list

# install vault
sudo apt update && sudo apt install vault

# Verification
vault version
```

## Stating the server

```bash
$ vault server -dev

...

You may need to set the following environment variables:

    $ export VAULT_ADDR='http://127.0.0.1:8200'

The unseal key and root token are displayed below in case you want to
seal/unseal the Vault or re-authenticate.

Unseal Key: Dkz/Ri7W187dpim4f8O6srPrMK0UeuJ4p7Puh4QPuBM=
Root Token: hvs.niVPGl8UvHMlsuhj88iKnrph

...
```

Set environment variables:
```bash
export VAULT_ADDR='http://127.0.0.1:8200'
export VAULT_TOKEN="hvs.niVPGl8UvHMlsuhj88iKnrph"
```

Save the unseal key somewhere.

Verify the server is running
```bash
vault status
```

## Google Cloud auth method

https://developer.hashicorp.com/vault/docs/auth/gcp

