#!/usr/bin/env bash

kubectl delete -f ../deployment.yaml
kubectl delete configmap licence-config
kubectl delete configmap template-config