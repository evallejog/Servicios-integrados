# syntax=docker/dockerfile:1

# ---------- Etapa 1: compilación de la SPA ----------
FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY index.html vite.config.js ./
COPY src ./src
RUN npm run build

# ---------- Etapa 2: nginx sin privilegios ----------
FROM nginxinc/nginx-unprivileged:stable-alpine
COPY nginx.conf.template /etc/nginx/templates/default.conf.template
COPY --from=build /app/dist /usr/share/nginx/html

# URLs internas de los microservicios (se sobrescriben en docker-compose y Kubernetes)
ENV CLIENTES_URL=http://clientes:8080 \
    PRODUCTOS_URL=http://productos:8080 \
    INVENTARIO_URL=http://inventario:8080 \
    VENTAS_URL=http://ventas:8080 \
    FACTURACION_URL=http://facturacion:8080

EXPOSE 8080
