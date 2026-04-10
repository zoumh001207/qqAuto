#!/usr/bin/env bash
set -euo pipefail

APP_NAME="${APP_NAME:-qqauto}"
APP_PORT="${APP_PORT:-8086}"
DEPLOY_DIR="${DEPLOY_DIR:-/zoumh/docker/qqauto}"
JAR_PATH="${JAR_PATH:-target/qqauto-0.0.1-SNAPSHOT.jar}"
APP_CONTEXT_PATH="${APP_CONTEXT_PATH:-/qqauto}"
MYSQL_CONTAINER="${MYSQL_CONTAINER:-mysql8}"
MYSQL_DATABASE="${MYSQL_DATABASE:-qqauto}"
MYSQL_USERNAME="${MYSQL_USERNAME:-root}"
MYSQL_PASSWORD="${MYSQL_PASSWORD:-zoumh}"

mkdir -p "${DEPLOY_DIR}/data"
docker exec "${MYSQL_CONTAINER}" mysql -u"${MYSQL_USERNAME}" -p"${MYSQL_PASSWORD}" -e "CREATE DATABASE IF NOT EXISTS \`${MYSQL_DATABASE}\` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

docker build -t "${APP_NAME}:latest" .
docker rm -f "${APP_NAME}" >/dev/null 2>&1 || true
docker run -d \
  --name "${APP_NAME}" \
  --restart unless-stopped \
  -p "${APP_PORT}:8086" \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SERVER_PORT=8086 \
  -e APP_CONTEXT_PATH="${APP_CONTEXT_PATH}" \
  -e SPRING_DATASOURCE_URL="jdbc:mysql://${MYSQL_CONTAINER}:3306/${MYSQL_DATABASE}?useUnicode=true&characterEncoding=utf8mb4&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai" \
  -e SPRING_DATASOURCE_USERNAME="${MYSQL_USERNAME}" \
  -e SPRING_DATASOURCE_PASSWORD="${MYSQL_PASSWORD}" \
  -v "${DEPLOY_DIR}/data:/app/data" \
  "${APP_NAME}:latest"

echo "deployed ${APP_NAME} on port ${APP_PORT}"
