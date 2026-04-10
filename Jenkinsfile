pipeline {
  agent any

  parameters {
    string(name: 'DEPLOY_HOST', defaultValue: '156.225.28.110', description: 'Deployment host')
    string(name: 'DEPLOY_DIR', defaultValue: '/zoumh/docker/qqauto', description: 'Deployment directory')
    string(name: 'CONTAINER_NAME', defaultValue: 'qqauto', description: 'Docker container name')
    string(name: 'HTTP_PORT', defaultValue: '8086', description: 'Container port exposed on host')
    string(name: 'APP_CONTEXT_PATH', defaultValue: '/qqauto', description: 'Servlet context path')
    string(name: 'MYSQL_CONTAINER', defaultValue: 'mysql8', description: 'MySQL container name')
    string(name: 'MYSQL_DATABASE', defaultValue: 'qqauto', description: 'MySQL database name')
    string(name: 'MYSQL_USERNAME', defaultValue: 'root', description: 'MySQL username')
    password(name: 'MYSQL_PASSWORD', defaultValue: 'zoumh', description: 'MySQL password')
  }

  stages {
    stage('Build') {
      steps {
        sh 'mvn -B clean package -DskipTests'
      }
    }

    stage('Docker Image') {
      steps {
        sh 'docker build -t qqauto:latest .'
      }
    }

    stage('Deploy') {
      steps {
        sh '''
          mkdir -p ${DEPLOY_DIR}
          docker exec ${MYSQL_CONTAINER} mysql -u${MYSQL_USERNAME} -p${MYSQL_PASSWORD} -e "CREATE DATABASE IF NOT EXISTS ${MYSQL_DATABASE} DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
          docker rm -f ${CONTAINER_NAME} || true
          docker run -d \
            --name ${CONTAINER_NAME} \
            --restart unless-stopped \
            -p ${HTTP_PORT}:8086 \
            -e SPRING_PROFILES_ACTIVE=prod \
            -e SERVER_PORT=8086 \
            -e APP_CONTEXT_PATH=${APP_CONTEXT_PATH} \
            -e SPRING_DATASOURCE_URL=jdbc:mysql://${MYSQL_CONTAINER}:3306/${MYSQL_DATABASE}?useUnicode=true\&characterEncoding=utf8\&useSSL=false\&allowPublicKeyRetrieval=true\&serverTimezone=Asia/Shanghai \
            -e SPRING_DATASOURCE_USERNAME=${MYSQL_USERNAME} \
            -e SPRING_DATASOURCE_PASSWORD=${MYSQL_PASSWORD} \
            -v ${DEPLOY_DIR}/data:/app/data \
            qqauto:latest
        '''
      }
    }
  }
}
