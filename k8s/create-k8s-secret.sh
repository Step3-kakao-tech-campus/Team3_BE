#!/bin/bash

# Set your Kubernetes secret name
SECRET_NAME="secrets"

if kubectl get secret $SECRET_NAME > /dev/null 2>&1; then
  # Secret exists, delete it
  kubectl delete secret $SECRET_NAME
fi

# Create or update the Kubernetes secret with environment variables
kubectl create secret generic $SECRET_NAME \
  --from-literal=TOKEN_SECRET="$TOKEN_SECRET" \
  --from-literal=GMAIL_USERNAME="$GMAIL_USERNAME" \
  --from-literal=GMAIL_APPLICATION_PASSWORD="$GMAIL_APPLICATION_PASSWORD" \
  --from-literal=AWS_S3_END_POINT="$AWS_S3_END_POINT" \
  --from-literal=AWS_ACCESS_KEY="$AWS_ACCESS_KEY" \
  --from-literal=AWS_SECRET_KEY="$AWS_SECRET_KEY" \
  --from-literal=MYSQL_ROOT_PASSWORD="$MYSQL_ROOT_PASSWORD" \
  --from-literal=MYSQL_USERNAME="$MYSQL_USERNAME" \
  --from-literal=MYSQL_PASSWORD="$MYSQL_PASSWORD" \
  --from-literal=DOMAIN="$DOMAIN" \
  --from-literal=GOOGLE_MAP_API_KEY="$GOOGLE_MAP_API_KEY" \
  --from-literal=API_SERVER_URL="$API_SERVER_URL" \
  --from-literal=NEXT_PUBLIC_KAKAOMAP_APPKEY="$NEXT_PUBLIC_KAKAOMAP_APPKEY"


echo "Kubernetes secret $SECRET_NAME has been created or updated with the environment variables."
