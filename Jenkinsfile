pipeline {
  agent any

  options {
    disableConcurrentBuilds()
  }

  environment {
    APP_NAME = 'qqauto'
    APP_PORT = '8086'
    APP_CONTEXT_PATH = '/qqauto'
    DEPLOY_DIR = '/zoumh/docker/qqauto'
    MYSQL_CONTAINER = 'mysql8'
    MYSQL_DATABASE = 'qqauto'
    MYSQL_USERNAME = 'root'
    MYSQL_PASSWORD = 'zoumh'
    NGINX_CONF = '/zoumh/data/nginx/conf/conf.d/zoumh.com.conf'
    MAVEN_CACHE = '/zoumh/data/jenkins/caches/maven'
    HOST_JENKINS_DIR = '/zoumh/data/jenkins'
    MAVEN_IMAGE = 'maven:3.9.9-eclipse-temurin-21'
  }

  stages {
    stage('Checkout') {
      steps {
        git branch: 'main', url: 'https://github.com/zoumh001207/qqAuto.git'
      }
    }

    stage('Build') {
      steps {
        sh '''
          set -e
          mkdir -p "$MAVEN_CACHE"
          docker run --rm \
            -u 0:0 \
            -v "${HOST_JENKINS_DIR}/workspace/${JOB_NAME}:/workspace" \
            -w /workspace \
            -v "$MAVEN_CACHE:/maven-repo" \
            -v "${HOST_JENKINS_DIR}/.m2/settings.xml:/root/.m2/settings.xml:ro" \
            "$MAVEN_IMAGE" \
            mvn -B clean package -DskipTests -Dmaven.repo.local=/maven-repo
        '''
      }
    }

    stage('Deploy App') {
      steps {
        sh '''
          set -e
          mkdir -p ${DEPLOY_DIR}
          docker exec ${MYSQL_CONTAINER} mysql -u${MYSQL_USERNAME} -p${MYSQL_PASSWORD} -e "CREATE DATABASE IF NOT EXISTS ${MYSQL_DATABASE} DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
          docker build -t ${APP_NAME}:latest .
          docker rm -f ${APP_NAME} >/dev/null 2>&1 || true
          docker run -d \
            --name ${APP_NAME} \
            --restart unless-stopped \
            -p ${APP_PORT}:8086 \
            -e SPRING_PROFILES_ACTIVE=prod \
            -e SERVER_PORT=8086 \
            -e APP_CONTEXT_PATH=${APP_CONTEXT_PATH} \
            -e SPRING_DATASOURCE_URL="jdbc:mysql://${MYSQL_CONTAINER}:3306/${MYSQL_DATABASE}?useUnicode=true&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai" \
            -e SPRING_DATASOURCE_USERNAME=${MYSQL_USERNAME} \
            -e SPRING_DATASOURCE_PASSWORD=${MYSQL_PASSWORD} \
            -v ${DEPLOY_DIR}/data:/app/data \
            ${APP_NAME}:latest
          MYSQL_NETWORK=$(docker inspect "$MYSQL_CONTAINER" --format '{{range $name, $_ := .NetworkSettings.Networks}}{{$name}} {{end}}' | awk '{print $1}')
          if [ -n "$MYSQL_NETWORK" ]; then
            docker network connect "$MYSQL_NETWORK" "${APP_NAME}" >/dev/null 2>&1 || true
          fi
          NGINX_CONTAINER=$(docker ps --format '{{.Names}} {{.Image}}' | awk '$1 ~ /nginx/ || $2 ~ /nginx/ { print $1; exit }')
          if [ -n "$NGINX_CONTAINER" ]; then
            NGINX_NETWORK=$(docker inspect "$NGINX_CONTAINER" --format '{{range $name, $_ := .NetworkSettings.Networks}}{{$name}} {{end}}' | awk '{print $1}')
            if [ -n "$NGINX_NETWORK" ]; then
              docker network connect "$NGINX_NETWORK" "${APP_NAME}" >/dev/null 2>&1 || true
            fi
          fi
        '''
      }
    }

    stage('Update Nginx') {
      steps {
        sh '''
          set -e
          NGINX_CONTAINER=$(docker ps --format '{{.Names}} {{.Image}}' | awk '$1 ~ /nginx/ || $2 ~ /nginx/ { print $1; exit }')
          if [ -z "$NGINX_CONTAINER" ]; then
            echo "No running nginx container found"
            docker ps --format 'table {{.Names}}\t{{.Image}}\t{{.Status}}'
            exit 1
          fi
          NGINX_NETWORK=$(docker inspect "$NGINX_CONTAINER" --format '{{range $name, $_ := .NetworkSettings.Networks}}{{$name}} {{end}}' | awk '{print $1}')
          if [ -z "$NGINX_NETWORK" ]; then
            echo "Unable to determine nginx container network"
            exit 1
          fi
          APP_UPSTREAM=$(docker network inspect "$NGINX_NETWORK" --format '{{range .Containers}}{{println .Name .IPv4Address}}{{end}}' | awk '$1=="'"${APP_NAME}"'" {print $2}' | cut -d/ -f1)
          if [ -z "$APP_UPSTREAM" ]; then
            echo "Unable to determine ${APP_NAME} IP on network $NGINX_NETWORK"
            docker inspect "${APP_NAME}"
            exit 1
          fi
          cp "$NGINX_CONF" "$NGINX_CONF.bak-$(date +%Y%m%d%H%M%S)"
          awk '
            BEGIN { skip = 0 }
            /location \\/qqauto\\// { skip = 1; next }
            skip && /^[[:space:]]*}[[:space:]]*$/ { skip = 0; next }
            !skip { print }
          ' "$NGINX_CONF" > "$NGINX_CONF.clean"
          head -n -1 "$NGINX_CONF.clean" > "$NGINX_CONF.tmp"
          cat >> "$NGINX_CONF.tmp" <<'EOF'
    location /qqauto/ {
        proxy_pass http://__APP_UPSTREAM__:8086/qqauto/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
EOF
          sed -i "s/__APP_UPSTREAM__/${APP_UPSTREAM}/g" "$NGINX_CONF.tmp"
          tail -n 1 "$NGINX_CONF.clean" >> "$NGINX_CONF.tmp"
          mv "$NGINX_CONF.tmp" "$NGINX_CONF"
          rm -f "$NGINX_CONF.clean"
          docker exec "$NGINX_CONTAINER" nginx -t
          docker exec "$NGINX_CONTAINER" nginx -s reload
        '''
      }
    }

    stage('Verify') {
      steps {
        sh '''
          set -e
          docker inspect -f '{{.State.Status}}' "${APP_NAME}" | grep -qx running
          APP_OK=0
          for i in $(seq 1 30); do
            if curl -kfsS "https://zoumh.com${APP_CONTEXT_PATH}/login" >/dev/null; then
              APP_OK=1
              break
            fi
            sleep 2
          done
          if [ "$APP_OK" -ne 1 ]; then
            docker ps -a --filter "name=${APP_NAME}" --format 'table {{.Names}}\t{{.Status}}\t{{.Ports}}'
            docker logs --tail 200 "${APP_NAME}" || true
            exit 1
          fi
        '''
      }
    }
  }
}
