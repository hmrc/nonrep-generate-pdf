#!/usr/bin/env bash
kubectl create configmap licence-config --from-file=../dito/config/dito_trial_HMRC.xml
kubectl create configmap template-config --from-file=../dito/work/NRS_PDF.dito
kubectl describe configmaps licence-config
kubectl describe configmaps template-config
kubectl apply -f ../deployment.yaml