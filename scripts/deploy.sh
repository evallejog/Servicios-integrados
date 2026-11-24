#!/usr/bin/env bash
# Despliega la plataforma en el clúster al que apunta el contexto actual de kubectl.
#
# Uso:
#   ACR_LOGIN_SERVER=acrserviciosintegrados.azurecr.io IMAGE_TAG=42 ./scripts/deploy.sh [namespace]
#
# Variable opcional: APLICAR_INGRESS=false omite el Ingress (útil si otro namespace ya publica la ruta "/").
# Prerrequisitos: kubectl, envsubst (paquete gettext) y el secreto "db-credentials" ya creado en el namespace.
set -euo pipefail

NAMESPACE="${1:-servicios-integrados}"
: "${ACR_LOGIN_SERVER:?Define ACR_LOGIN_SERVER (por ejemplo acrserviciosintegrados.azurecr.io)}"
: "${IMAGE_TAG:?Define IMAGE_TAG (por ejemplo el número de build)}"

export NAMESPACE ACR_LOGIN_SERVER IMAGE_TAG
RAIZ="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SERVICIOS=(clientes productos inventario ventas facturacion)

aplicar() {
  envsubst '${NAMESPACE} ${ACR_LOGIN_SERVER} ${IMAGE_TAG}' < "$1" | kubectl apply -f -
}

echo "==> Namespace ${NAMESPACE}"
aplicar "${RAIZ}/kubernetes/00-namespace.yaml"

if ! kubectl -n "${NAMESPACE}" get secret db-credentials >/dev/null 2>&1; then
  echo "ERROR: falta el secreto db-credentials en el namespace ${NAMESPACE}." >&2
  echo "Créalo con: kubectl -n ${NAMESPACE} create secret generic db-credentials \\" >&2
  echo "  --from-literal=POSTGRES_USER=servicios --from-literal=POSTGRES_PASSWORD='<contraseña>'" >&2
  exit 1
fi

echo "==> Base de datos"
aplicar "${RAIZ}/kubernetes/postgres.yaml"
kubectl -n "${NAMESPACE}" rollout status statefulset/postgres --timeout=180s

echo "==> Microservicios, frontend, escalado e ingress"
for archivo in "${SERVICIOS[@]}" frontend hpa; do
  aplicar "${RAIZ}/kubernetes/${archivo}.yaml"
done
if [[ "${APLICAR_INGRESS:-true}" == "true" ]]; then
  aplicar "${RAIZ}/kubernetes/ingress.yaml"
fi

echo "==> Esperando a que los despliegues estén listos"
for despliegue in "${SERVICIOS[@]}" frontend; do
  kubectl -n "${NAMESPACE}" rollout status "deployment/${despliegue}" --timeout=300s
done

echo "==> Prueba de humo: salud de cada microservicio dentro del clúster"
kubectl -n "${NAMESPACE}" run "humo-$$" --rm -i --restart=Never --image=curlimages/curl:8.10.1 --command -- \
  sh -c 'for s in clientes productos inventario ventas facturacion; do
           printf "%s: " "$s"; curl -fsS --max-time 5 "http://$s/actuator/health" || exit 1; echo
         done'

echo "==> Despliegue completado en ${NAMESPACE}"
kubectl -n "${NAMESPACE}" get pods,svc,ingress || true
