pipeline {
  agent any

  parameters {
    string(name: 'DEPLOY_HOST', defaultValue: '156.225.28.110', description: 'Deployment host')
    string(name: 'DEPLOY_DIR', defaultValue: '/zoumh/docker/qqauto', description: 'Deployment directory')
    string(name: 'CONTAINER_NAME', defaultValue: 'qqauto', description: 'Docker container name')
    string(name: 'HTTP_PORT', defaultValue: '8086', description: 'Container port exposed on host')
    string(name: 'APP_CONTEXT_PATH', defaultValue: '/qqauto', description: 'Servlet context path')
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
          docker rm -f ${CONTAINER_NAME} || true
          docker run -d \
            --name ${CONTAINER_NAME} \
            --restart unless-stopped \
            -p ${HTTP_PORT}:8086 \
            -e APP_CONTEXT_PATH=${APP_CONTEXT_PATH} \
            -v ${DEPLOY_DIR}/data:/app/data \
            qqauto:latest
        '''
      }
    }
  }
}
