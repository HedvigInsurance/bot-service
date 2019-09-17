#!/usr/bin/env bash

if [ -z "${TRAVIS_PULL_REQUEST}" ] || [ "${TRAVIS_PULL_REQUEST}" == "false" ]; then
  if [ "${TRAVIS_BRANCH}" == "master" ]; then
    aws s3 cp s3://dev-com-hedvig-cluster-ett-data/kube ~/.kube --recursive
    
    curl -LO https://storage.googleapis.com/kubernetes-release/release/$(curl -s https://storage.googleapis.com/kubernetes-release/release/stable.txt)/bin/linux/amd64/kubectl
    chmod +x ./kubectl
    chmod +x ~/.kube/heptio-authenticator-aws
    
    ./kubectl set image deployment/bot-service bot-service=$REMOTE_IMAGE_URL:${TRAVIS_COMMIT}
  else
    echo "Skipping deploy because branch is not master"
  fi
else
  echo "Skipping deploy because it's a pull request"
fi
