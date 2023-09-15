# Terraform

## Installation

```bash
# installing gnupg and software-properties-common
sudo apt-get update && sudo apt-get install -y gnupg software-properties-common

# install HashiCorp GPG key
wget -O- https://apt.releases.hashicorp.com/gpg | \
gpg --dearmor | \
sudo tee /usr/share/keyrings/hashicorp-archive-keyring.gpg

# Verify the key's fingerprint
gpg --no-default-keyring \
--keyring /usr/share/keyrings/hashicorp-archive-keyring.gpg \
--fingerprint

# Add the official HashiCorp repository
echo "deb [signed-by=/usr/share/keyrings/hashicorp-archive-keyring.gpg] \
https://apt.releases.hashicorp.com $(lsb_release -cs) main" | \
sudo tee /etc/apt/sources.list.d/hashicorp.list

# Download the package information from HashiCorp
sudo apt update

# Install Terraform from the new repository
sudo apt-get install terraform

# Verification
terraform -help
```

## Installing autocomplete

```
terraform -install-autocomplete
```

## Command workflow

1. create `main.tf`
2. run `terraform init`
3. run `terraform fmt`
4. run `terraform plan`
5. run `terraform apply`
6. run `terraform show`
7. run `terraform destroy`
