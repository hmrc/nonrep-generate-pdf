#!/usr/bin/env bash
kubectl create configmap licence-config --from-file=../dito/config/dito_trial_HMRC.xml
kubectl describe configmaps licence-config
kubectl apply -f ../deployment.yaml