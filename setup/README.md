# Setup

## Create a Ubuntu VM

<details><summary style="color:Maroon;font-size:16px;">Show Contents</summary>

- Machine type: e2-standard-4
- Architecture: x86/64
- Boot disk
    - Image: ubuntu-20.04-lts
    - Type: SSD persistent disk
    - Size: 100 GB
- Allow full access to all Cloud APIs
- HTTP traffic: On
- HTTPS traffic: On

</details>

## Install Software

<details><summary style="color:Maroon;font-size:16px;">Show Contents</summary>

### Docker engine 

<details><summary style="color:Maroon;font-size:16px;">Show Contents</summary>

Docker engine is the only prerequisites for minikube. Use 
[this](https://docs.docker.com/engine/install/ubuntu/#uninstall-docker-engine)
as reference.

1. Uninstall old versions
    ```bash
    for pkg in docker.io docker-doc docker-compose podman-docker containerd runc; \
    do sudo apt-get remove $pkg; done
    sudo rm -rf /var/lib/docker
    sudo rm -rf /var/lib/containerd
    ```

2. Update the apt package index and install packages to allow apt to use a repository over HTTPS:
    ```bash
    sudo apt-get update
    sudo apt-get install ca-certificates curl gnupg
    ```

3. Add Docker’s official GPG key:
    ```bash
    sudo install -m 0755 -d /etc/apt/keyrings
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
    sudo chmod a+r /etc/apt/keyrings/docker.gpg
    ```
3. Use the following command to set up the repository:
    ```bash
    echo \
    "deb [arch="$(dpkg --print-architecture)" signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
    "$(. /etc/os-release && echo "$VERSION_CODENAME")" stable" | \
    sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
    ```
4. Update the apt package index:
    ```bash
    sudo apt-get update
    ```
5. Install the latest Docker Engine, containerd, and Docker Compose.
    ```bash
    sudo apt-get install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
    ```
6. Verify that the Docker Engine installation is successful by running the `hello-world` image.
    ```bash
    sudo docker run hello-world
    ```

</details>

### minikube

<details><summary style="color:Maroon;font-size:16px;">Show Contents</summary>

1. To download the latest minikube stable release on x86-64 Linux using binary download:
    ```bash
    curl -LO https://storage.googleapis.com/minikube/releases/latest/minikube-linux-amd64
    ```
2. Install 
    ```bash
    sudo install minikube-linux-amd64 /usr/local/bin/minikube
    ```
3. Start the cluster
    ```bash
    sudo usermod -aG docker $USER && newgrp docker
    minikube start --driver=docker
    ```
4. Let the minikube can download the appropriate version of kubectl:
    ```bash
    minikube kubectl -- get po -A
    ```
5. Make docker the default driver:
    ```bash
    minikube config set driver docker
    ```
5. Create an alias
    ```bash
    echo 'alias kubectl="minikube kubectl --"' >> ~/.bashrc
    echo 'alias k="minikube kubectl --"' >> ~/.bashrc
    source ~/.bashrc
    ```
6. Create a sample deployment
    ```bash
    kubectl create deployment hello-minikube --image=kicbase/echo-server:1.0
    kubectl get pods
    ```

</details>

### python 3.8

<details><summary style="color:Maroon;font-size:16px;">Show Contents</summary>
```bash
sudo apt update
sudo apt install software-properties-common
sudo apt autoremove
sudo add-apt-repository ppa:deadsnakes/ppa
sudo apt update
sudo apt install python3.8
python3 --version
echo "alias python='python3'" >> ~/.bashrc
source ~/.bashrc
```

</details>

### Kubectl

<details><summary style="color:Maroon;font-size:16px;">Show Contents</summary>
Kubectl is required by other packages such as Knative.

1. Download the latest version
    ```bash
    curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
    ```
2. Install
    ```bash
    sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl
    ```
3. Verify the installation
    ```bash
    kubectl version --client
    ```

</details>

### Google Cloud SDK

<details><summary style="color:Maroon;font-size:16px;">Show Contents</summary>
1. Removed the preinstalled gcloud
    ```bash
    sudo /usr/bin/snap remove google-cloud-cli
    ```
2. Install gcloud cli (reference: https://cloud.google.com/sdk/docs/install#linux)
    ```bash
    curl -O https://dl.google.com/dl/cloudsdk/channels/rapid/downloads/google-cloud-cli-437.0.0-linux-x86_64.tar.gz
    tar xf google-cloud-cli-437.0.0-linux-x86_64.tar.gz
    ./google-cloud-sdk/install.sh
    # enable auto-completion
    source ~/.bashrc
    gcloud components install beta
    gcloud components install cloud-run-proxy
    ```
3. Config gcloud
    ```bash
    gcloud config set project ibcwe-event-layer-f3ccf6d9
    gcloud config set run/region us-central1
    ```

</details>

### golang 1.20

<details><summary style="color:Maroon;font-size:16px;">Show Contents</summary>
```bash
wget https://go.dev/dl/go1.20.5.linux-amd64.tar.gz
tar xvf go1.20.5.linux-amd64.tar.gz
sudo mv go /usr/local
echo 'export GOROOT=/usr/local/go' >> ~/.bashrc
echo 'export GOPATH=$HOME/go' >> ~/.bashrc
echo 'export PATH=$GOPATH/bin:$GOROOT/bin:$PATH' >> ~/.bashrc
source ~/.bashrc
go version
```

</details>

### JDK 17

<details><summary style="color:Maroon;font-size:16px;">Show Contents</summary>

```bash
# Ensure the necessary packages are present:
apt install -y wget apt-transport-https

# Download the Eclipse Adoptium GPG key:
mkdir -p /etc/apt/keyrings
wget -O - https://packages.adoptium.net/artifactory/api/gpg/key/public | sudo tee /etc/apt/keyrings/adoptium.asc

# Configure the Eclipse Adoptium apt repository. To check the full list of 
# versions supported take a look at the list in the tree at 
# https://packages.adoptium.net/ui/native/deb/dists/.
#
# For Linux Mint (based on Ubuntu) you have to replace VERSION_CODENAME with UBUNTU_CODENAME.
echo "deb [signed-by=/etc/apt/keyrings/adoptium.asc] https://packages.adoptium.net/artifactory/deb $(awk -F= '/^VERSION_CODENAME/{print$2}' /etc/os-release) main" | sudo tee /etc/apt/sources.list.d/adoptium.list
 	
# Install the Temurin version you require:
apt update 
apt install temurin-17-jdk
```

</details>

### gradle

<details><summary style="color:Maroon;font-size:16px;">Show Contents</summary>

```bash
curl -L https://services.gradle.org/distributions/gradle-7.6.2-bin.zip -o gradle.zip
sudo mkdir /opt/gradle
sudo unzip -d /opt/gradle gradle.zip
echo 'export PATH=$PATH:/opt/gradle/gradle-7.6.2/bin' >> ~/.bashrc
source ~/.bashrc
```
</details>

### mkcert

<details><summary style="color:Maroon;font-size:16px;">Show Contents</summary>

```bash
sudo apt install libnss3-tools
curl -JLO "https://dl.filippo.io/mkcert/latest?for=linux/amd64"
chmod +x mkcert-v*-linux-amd64
sudo cp mkcert-v*-linux-amd64 /usr/local/bin/mkcert
rm mkcert-v*-linux-amd64
```

</details>

</details>


<style>
    h1 {
        color: DarkRed;
        text-align: center;
    }
    h2 {
        color: DarkBlue;
    }
    h3 {
        color: DarkGreen;
    }
    h4 {
        color: DarkMagenta;
    }
    strong {
        color: Maroon;
    }
    em {
        color: Maroon;
    }
    img {
        display: block;
        margin-left: auto;
        margin-right: auto
    }
    code {
        color: SlateBlue;
    }
    mark {
        background-color:GoldenRod;
    }
</style>