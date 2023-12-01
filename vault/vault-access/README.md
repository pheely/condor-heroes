# Accessing Hashicorp Vault

## Setup

<details><summary style="color:Maroon;font-size:16px;">Show Contents</summary>

### GCP 
<details><summary style="color:Maroon;font-size:16px;">Show Contents</summary>

A service account with the following permissions, and its JSON key file are required:
- iam.serviceAccounts.get
- iam.serviceAccountKeys.get
- compute.instances.get
- compute.instanceGroups.list
- iam.serviceAccounts.signJwt

</details>

### Vault

<details><summary style="color:Maroon;font-size:16px;">Show Contents</summary>

1. Enable the GCP secrets engine
2. Configure the GCP auth method to use the key file
3. Create a policy that associated with the GCP auth method: read and list secrets
4. Create a role for specific GCE instances, e.g. GCP project, zone.

</details>

</details>

## Authentication to Vault from a GCE

<details><summary style="color:Maroon;font-size:16px;">Show Contents</summary>

1. Create a GCE instance in the specified GCP project and zone
2. SSH to the VM
3. Connect and retrieve secrets using CLI
4. Connect and retrieve secrets in a Java app
    
</details>

