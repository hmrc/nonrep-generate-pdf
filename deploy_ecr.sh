#!/usr/bin/env bash

set -e

if [ "$#" -ne 1 ]
then
    echo "ECR repository url not provided, this must be the first and only argument"
    exit 1
fi
if [ ! -f version.txt ]; then
    echo "version.txt is required in this directory before running this script, to generate run 'sbt createVersionFile'"
    exit 1
fi

REPO_URL=$1
family=generate-pdf
version=`cat version.txt`

aws ecr get-login | sh

docker build -f Dockerfile -t $family .

docker tag $family $family:$version
docker tag $family:$version $REPO_URL:$version
docker push $REPO_URL:$version

docker tag $family:latest $REPO_URL:latest
docker push $REPO_URL:latest