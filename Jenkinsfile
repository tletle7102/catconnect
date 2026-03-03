pipeline {
    agent any

    environment {
        CATCONNECT_SPRING_PROFILE_ACTIVE = credentials('catconnect-spring-profile-active')
        CATCONNECT_TOMCAT_PORT = credentials('catconnect-tomcat-port')
        CATCONNECT_SPRING_SECURITY_JWT_SECRET = credentials('catconnect-jwt-secret')
        CATCONNECT_SPRING_SECURITY_EXPIRATION = credentials('catconnect-jwt-expiration')
        CATCONNECT_DEV_DB_URL = credentials('catconnect-dev-db-url')
        CATCONNECT_DEV_DB_USERNAME = credentials('catconnect-dev-db-username')
        CATCONNECT_DEV_DB_PASSWORD = credentials('catconnect-dev-db-password')
        CATCONNECT_DEV_DB_NAME = credentials('catconnect-dev-db-name')
        MAIL_USERNAME = credentials('catconnect-mail-username')
        MAIL_PASSWORD = credentials('catconnect-mail-password')
        SOLAPI_API_KEY = credentials('catconnect-solapi-api-key')
        SOLAPI_API_SECRET = credentials('catconnect-solapi-api-secret')
        SOLAPI_SENDER_PHONE = credentials('catconnect-solapi-sender-phone')

        DOCKER_IMAGE = 'catconnect'
        DOCKER_CONTAINER_NAME = 'catconnect-container'
    }

    stages {

        stage('Clone Repository') {
            steps {
                git credentialsId: 'github-credentials',
                    url: 'https://github.com/tletle7102/catconnect.git',
                    branch: 'main'
            }
        }

        stage('Build Gradle') {
            steps {
                sh '''
                chmod +x gradlew
                ./gradlew clean build
                '''
            }
        }

        stage('Build Docker Image') {
            steps {
                sh '''
                docker build -t ${DOCKER_IMAGE} .
                '''
            }
        }

        stage('Deploy Docker Container') {
            steps {
                sh '''
                docker rm -f ${DOCKER_CONTAINER_NAME} || true

                docker run -d \
                --restart unless-stopped \
                -p "$CATCONNECT_TOMCAT_PORT:$CATCONNECT_TOMCAT_PORT" \
                --name ${DOCKER_CONTAINER_NAME} \
                -e "CATCONNECT_SPRING_PROFILE_ACTIVE=$CATCONNECT_SPRING_PROFILE_ACTIVE" \
                -e "CATCONNECT_TOMCAT_PORT=$CATCONNECT_TOMCAT_PORT" \
                -e "CATCONNECT_SPRING_SECURITY_JWT_SECRET=$CATCONNECT_SPRING_SECURITY_JWT_SECRET" \
                -e "CATCONNECT_SPRING_SECURITY_EXPIRATION=$CATCONNECT_SPRING_SECURITY_EXPIRATION" \
                -e "CATCONNECT_DEV_DB_URL=$CATCONNECT_DEV_DB_URL" \
                -e "CATCONNECT_DEV_DB_USERNAME=$CATCONNECT_DEV_DB_USERNAME" \
                -e "CATCONNECT_DEV_DB_PASSWORD=$CATCONNECT_DEV_DB_PASSWORD" \
                -e "CATCONNECT_DEV_DB_NAME=$CATCONNECT_DEV_DB_NAME" \
                -e "MAIL_USERNAME=$MAIL_USERNAME" \
                -e "MAIL_PASSWORD=$MAIL_PASSWORD" \
                -e "SOLAPI_API_KEY=$SOLAPI_API_KEY" \
                -e "SOLAPI_API_SECRET=$SOLAPI_API_SECRET" \
                -e "SOLAPI_SENDER_PHONE=$SOLAPI_SENDER_PHONE" \
                ${DOCKER_IMAGE}

                echo "Container started successfully"
                docker ps | grep ${DOCKER_CONTAINER_NAME}
                '''
            }
        }

    }

    post {
        success {
            echo 'Deploy SUCCESS'
        }
        failure {
            echo 'Deploy FAILED'
        }
    }
}