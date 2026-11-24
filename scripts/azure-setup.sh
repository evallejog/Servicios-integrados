#!/usr/bin/env bash
# Crea la infraestructura base en Azure: grupo de recursos, Azure Container Registry y clúster AKS.
# Requisitos: Azure CLI (az) con sesión iniciada (az login) y la suscripción correcta seleccionada.
#
# Uso:  ./scripts/azure-setup.sh
# Todas las variables se pueden sobrescribir desde el entorno, por ejemplo:
#   LOCATION=eastus2 ACR_NAME=miacrunico ./scripts/azure-setup.sh
set -euo pipefail

RESOURCE_GROUP="${RESOURCE_GROUP:-rg-ServiciosIntegrados}"
LOCATION="${LOCATION:-eastus}"
# El nombre del ACR es global en Azure: solo minúsculas y números, de 5 a 50 caracteres (sin guiones).
ACR_NAME="${ACR_NAME:-acrserviciosintegrados}"
AKS_NAME="${AKS_NAME:-aks-ServiciosIntegrados}"
NODE_COUNT="${NODE_COUNT:-2}"
NODE_VM_SIZE="${NODE_VM_SIZE:-Standard_B2s}"

echo "==> Grupo de recursos: ${RESOURCE_GROUP} (${LOCATION})"
az group create --name "${RESOURCE_GROUP}" --location "${LOCATION}" --output none

echo "==> Azure Container Registry: ${ACR_NAME}"
az acr create \
  --resource-group "${RESOURCE_GROUP}" \
  --name "${ACR_NAME}" \
  --sku Basic \
  --output none

echo "==> Clúster AKS: ${AKS_NAME} (${NODE_COUNT} nodos ${NODE_VM_SIZE})"
az aks create \
  --resource-group "${RESOURCE_GROUP}" \
  --name "${AKS_NAME}" \
  --node-count "${NODE_COUNT}" \
  --node-vm-size "${NODE_VM_SIZE}" \
  --attach-acr "${ACR_NAME}" \
  --generate-ssh-keys \
  --output none

echo "==> Habilitando el controlador de Ingress administrado (Application Routing)"
az aks approuting enable --resource-group "${RESOURCE_GROUP}" --name "${AKS_NAME}" --output none

echo "==> Credenciales de kubectl"
az aks get-credentials --resource-group "${RESOURCE_GROUP}" --name "${AKS_NAME}" --overwrite-existing

cat <<MENSAJE

Infraestructura lista.
  Registro de imágenes : ${ACR_NAME}.azurecr.io
  Clúster AKS          : ${AKS_NAME}

Siguientes pasos:
  1. Crear el secreto de base de datos (ver README, sección "Despliegue en AKS").
  2. Configurar las service connections y los environments en Azure DevOps (ver README).
  3. Ejecutar el pipeline, o desplegar manualmente con scripts/deploy.sh.
MENSAJE
