#!/usr/bin/env bash
set -euo pipefail

APP_NAME="${APP_NAME:-qqauto}"
APP_PORT="${APP_PORT:-8086}"
DEPLOY_DIR="${DEPLOY_DIR:-/zoumh/docker/qqauto}"
JAR_PATH="${JAR_PATH:-target/qqauto-0.0.1-SNAPSHOT.jar}"
APP_CONTEXT_PATH="${APP_CONTEXT_PATH:-/qqauto}"

mkdir -p "${DEPLOY_DIR}/data"

docker build -t "${APP_NAME}:latest" .
docker rm -f "${APP_NAME}" >/dev/null 2>&1 || true
docker run -d \
  --name "${APP_NAME}" \
  --restart unless-stopped \
  -p "${APP_PORT}:8086" \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SERVER_PORT=8086 \
  -e APP_CONTEXT_PATH="${APP_CONTEXT_PATH}" \
  -v "${DEPLOY_DIR}/data:/app/data" \
  "${APP_NAME}:latest"

echo "deployed ${APP_NAME} on port ${APP_PORT}"
