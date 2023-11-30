# CI/CD in GCP

## Continuous Integration Pipeline

```bash
# create a source repository
gcloud source repos create hstcld-poc-repo

# clone the repo
gcloud source repos clone hstcld-poc-repo

# add files
cd hstcld-poc-repo
# create files

# commit and push to the remote repo
git add --all
git config --global user.email "you@example.com"
git config --global user.name "Your Name"
git commit -a -m "Initial Commit"
git push origin master

# create an artifact repo
gcloud artifacts repositories create cloud-run-try \
    --repository-format=docker \
    --location=us-central1

# get the access to the repo
gcloud auth configure-docker us-central1-docker.pkg.dev

# Start a manual build
gcloud builds submit \
  --tag us-central1-docker.pkg.dev/$DEVSHELL_PROJECT_ID/cloud-run-try/employee:v1.0 .

#create a cloud build trigger
# the following command is not working. need to define the trigger in the console.
gcloud builds triggers create cloud-source-repositories \
  --name=hstcld-poc-trigger  \
  --branch-pattern=.* \
  --dockerfile-image=us-central1-docker.pkg.dev/ibcwe-event-layer-f3ccf6d9/cloud-run-try/employee:$COMMIT_SHA \
  --dockerfile=Dockerfile \
  --repo hstcld-poc-repo

# run the trigger
gcloud builds triggers run hstcld-poc-trigger

# auto trigger
git commit -m "trigger build" --allow-empty
git push
```
